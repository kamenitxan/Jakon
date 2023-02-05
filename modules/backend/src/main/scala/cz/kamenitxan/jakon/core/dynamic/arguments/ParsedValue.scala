package cz.kamenitxan.jakon.core.dynamic.arguments

import java.lang.reflect.Field

/**
 * Created by TPa on 05.02.2023.
 */
case class ParsedValue(
												stringValue: String,
												seqObject: Seq[Seq[(Field, ParsedValue)]],
												seqValue: Seq[String],
												objectValue: Map[Field, ParsedValue]
											)
