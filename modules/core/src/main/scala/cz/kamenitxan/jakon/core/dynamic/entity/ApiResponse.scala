package cz.kamenitxan.jakon.core.dynamic.entity

/**
 * Created by Kamenitxan on 07.05.2025
 */
trait ApiResponse[T] {
	val status: ResponseStatus
	val data: T
}