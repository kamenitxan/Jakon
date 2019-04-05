package example.dynamic

import javax.validation.constraints.{Min, NotNull}

class PageletData {
	@NotNull(message = "NAME_NOT_NULL")
	@Min(10)
	var name: String = _
}
