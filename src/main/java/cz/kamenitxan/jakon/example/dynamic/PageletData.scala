package cz.kamenitxan.jakon.example.dynamic

import javax.validation.constraints.{Min, NotNull}

class PageletData {
	@NotNull
	@Min(10)
	var name: String = _
}
