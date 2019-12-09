package cz.kamenitxan.jakon.validation.validators;

import cz.kamenitxan.jakon.validation.ValidatedBy;
import cz.kamenitxan.jakon.webui.entity.MessageSeverity;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Year;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be an instant, date or time in the present or in the future.
 * <p>
 *
 * The notion of present here is defined relatively to the type on which the constraint is
 * used. For instance, if the constraint is on a {@link Year}, present would mean the whole
 * current year.
 * <p>
 * Supported types are:
 * <ul>
 *     <li>{@code java.time.Instant}</li>
 *     <li>{@code java.time.LocalDate}</li>
 *     <li>{@code java.time.LocalDateTime}</li>
 *     <li>{@code java.time.LocalTime}</li>
 *     <li>{@code java.time.MonthDay}</li>
 *     <li>{@code java.time.OffsetDateTime}</li>
 *     <li>{@code java.time.OffsetTime}</li>
 *     <li>{@code java.time.Year}</li>
 *     <li>{@code java.time.YearMonth}</li>
 *     <li>{@code java.time.ZonedDateTime}</li>
 *     <li>{@code java.time.chrono.HijrahDate}</li>
 *     <li>{@code java.time.chrono.JapaneseDate}</li>
 *     <li>{@code java.time.chrono.MinguoDate}</li>
 *     <li>{@code java.time.chrono.ThaiBuddhistDate}</li>
 * </ul>
 * <p>
 * {@code null} elements are considered valid.
 */
@Target({FIELD})
@Retention(RUNTIME)
@ValidatedBy(DummyValidator.class)
public @interface FutureOrPresent {
	MessageSeverity severity() default MessageSeverity.ERROR;

}
