package cz.kamenitxan.jakon.shop.payments

import cz.kamenitxan.jakon.shop.entity.PaymentMethod
import io.github.classgraph.ClassGraph

import java.lang.reflect.Modifier
import scala.jdk.CollectionConverters.*

/**
 * Classpath-scanning registry that maps gateway codes to [[PaymentGateway]] implementations.
 *
 * On first access the registry scans all classes under the {@code payments} package, instantiates
 * every concrete (non-abstract, non-interface) [[PaymentGateway]] implementation and indexes them
 * by their [[PaymentGateway#gatewayCode]]. Scala singleton objects are obtained via their
 * {@code MODULE$} field; regular classes are instantiated using a no-arg constructor.
 *
 * To register a new gateway, simply place its implementation class anywhere under the
 * {@code cz.kamenitxan.jakon.shop.payments} package tree — no explicit registration is needed.
 */
object PaymentGatewayRegistry {

	/**
	 * Lazily-initialised map of normalised gateway code → [[PaymentGateway]] instance.
	 * Throws [[IllegalStateException]] if two implementations share the same gateway code.
	 */
	lazy val gateways: Map[String, PaymentGateway] = discoverGateways()
		.map(gateway => normalizeCode(gateway.gatewayCode) -> gateway)
		.groupBy(_._1)
		.map {
			case (gatewayCode, implementations) if implementations.size == 1 => gatewayCode -> implementations.head._2
			case (gatewayCode, _) => throw new IllegalStateException(s"Multiple payment gateways registered for code '$gatewayCode'.")
		}

	/**
	 * Resolves the gateway associated with the given payment method.
	 *
	 * @param paymentMethod the payment method selected for the order
	 * @return the matching [[PaymentGateway]]
	 * @throws PaymentGatewayException if no gateway is registered for the payment method's code
	 */
	def resolve(paymentMethod: PaymentMethod): PaymentGateway = {
		val gatewayCode = paymentMethodGatewayCode(paymentMethod)
		gateways.getOrElse(gatewayCode, throw new PaymentGatewayException(s"Unsupported payment gateway '$gatewayCode' for payment method '${paymentMethod.name}'."))
	}

	/**
	 * Returns the normalised gateway code for the given payment method.
	 *
	 * @param paymentMethod the payment method to inspect
	 * @return lower-cased, trimmed gateway code; falls back to [[PaymentGatewayCode.Manual]] if blank
	 * @throws IllegalArgumentException if {@code paymentMethod} is {@code null}
	 */
	def paymentMethodGatewayCode(paymentMethod: PaymentMethod): String = {
		if (paymentMethod == null) {
			throw new IllegalArgumentException("Payment method must not be null.")
		}
		normalizeCode(paymentMethod.gatewayCode)
	}

	/**
	 * Normalises a raw gateway code to lower-case, trimmed form.
	 * Returns [[PaymentGatewayCode.Manual]] for blank or {@code null} input.
	 */
	private[payments] def normalizeCode(code: String): String = {
		Option(code).map(_.trim.toLowerCase).filter(_.nonEmpty).getOrElse(PaymentGatewayCode.Manual)
	}

	/** Scans the payments package and instantiates all concrete [[PaymentGateway]] implementations. */
	private def discoverGateways(): Seq[PaymentGateway] = {
		val scanResult = new ClassGraph()
			.enableClassInfo()
			.acceptPackages(classOf[PaymentGateway].getPackageName)
			.scan()

		try {
			scanResult.getClassesImplementing(classOf[PaymentGateway].getName)
				.loadClasses()
				.asScala
				.toSeq
				.filterNot(clazz => clazz.isInterface || Modifier.isAbstract(clazz.getModifiers))
				.map(instantiateGateway)
		} finally {
			scanResult.close()
		}
	}

	/**
	 * Obtains a [[PaymentGateway]] instance from a class.
	 * Prefers Scala's {@code MODULE$} singleton field; falls back to a no-arg constructor.
	 */
	private def instantiateGateway(clazz: Class[_]): PaymentGateway = {
		try {
			clazz.getField("MODULE$").get(null).asInstanceOf[PaymentGateway]
		} catch {
			case _: NoSuchFieldException =>
				clazz.getDeclaredConstructor().newInstance().asInstanceOf[PaymentGateway]
		}
	}
}
