package cz.kamenitxan.jakon.core.dynamic;

import java.lang.annotation.*;

/**
 * Example of simple pagelet
 * <pre>
 * &#64;Pagelet(path = "example")
 * class AttendancePagelet extends AbstractPagelet{
 *
 *    &#64;Get(path = "/", template = "ExamplePagelet")
 * 	  def get(req: Request, res: Response): Unit = {
 * 	 	???
 *    }
 *
 *    &#64;Post(path = "/", template = "ExamplePagelet")
 * 	  def post(req: Request, res: Response, data: ExamplePageletData): mutable.Map[String, Any] = {
 * 		???
 * 		redirect(req, res, "/svatba")
 *    }
 * }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Pagelet {
	String path() default "/";

	boolean authRequired() default false;

	boolean showInAdmin() default false;
}
