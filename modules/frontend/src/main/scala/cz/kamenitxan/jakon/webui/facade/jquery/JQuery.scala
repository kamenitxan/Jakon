package cz.kamenitxan.jakon.webui.facade

import cz.kamenitxan.jakon.webui.facade.jquery.{AttrVal, ElementDesc, Selector}

import scala.scalajs.js
import js.UndefOr
import js.|
import js.annotation.JSName
import org.scalajs.dom
import dom.Element

/**
 * A facade for the main jQuery object.
 * 
 * This is a reimplementation, very loosely based on the existing scalajs-jquery. It aims to be much
 * more strongly and precisely typed, while being as literal a translation of the functionality of jQuery
 * as possible. It is intentionally pretty close to scalajs-jquery, and many files can be switched over
 * by simply switching the import, but compatibility has *not* been a priority, and a modest number of
 * functions have changed in breaking ways. (This is one reason why I treated this as a rewrite rather
 * than as an evolution of the existing library.)
 * 
 * TODO: as of this writing, this is *quite* incomplete; I am only adding functions as I use them, and
 * at least half of them are currently missing. Pull requests are greatly welcomed. In particular, we are
 * lacking many overloads -- I've added some of them, but many jQuery functions have a considerable number
 * of potential overloads.
 * 
 * Many parameters are polymorphic. We often use | (type union) to define these, but | and UndefOr don't mix,
 * so we often have to spell things out in more detail. Also, you can't use a js.Function or js.ThisFunction
 * in a | expression, because it interferes with the compiler's implicit conversion from a Scala function
 * to a js.Function. Note that there are several common unions such as Selector defined in package.scala.
 * 
 * Things are also often spelled out more explicitly than you might expect, because Scala restricts us to
 * one overload per method with default parameters; this limits our usage of UndefOr.
 * 
 * We don't necessarily spell out every possible overload here, although we've made a serious effort to
 * make every version of the JS calls possible. In some cases, we only have versions that involve some
 * extra parameters. If you find yourself really wanting an overload that takes fewer parameters, pull
 * requests are welcome.
 * 
 * NOTE: discussion on scalajs Gitter, 1/28/15, says that facades should *return* Any, but
 * *take* js.Any *if* the Javascript is going to process the value in any way. This is the guiding principle here.
 * 
 * Also: when a facade function takes a property bag, if it is understood to be name/value pairs
 * in JS, declare it as js.Dictionary[T]. Often, we can constrain T; if not, just put js.Any, and it
 * is at least explicit that it is name/value pairs.
 * 
 * Long-deprecated functions are, by and large, simply omitted. Please see the jQuery documentation to see
 * what to use instead.
 */
@js.native
trait JQuery extends js.Object {
  
  def modal(action: String):JQuery = js.native
  
  /**
   * Create a new jQuery object with elements added to the set of matched elements.
   */
  def add(selector:ElementDesc):JQuery = js.native
  def add(selector:String, context:Element):JQuery = js.native
  
  /**
   * Add the previous set of elements on the stack to the current set, optionally filtered by a selector.
   */
  def addBack(selector:String = ???):JQuery = js.native
  
  /**
   * Adds the specified class(es) to each of the set of matched elements.
   */
  def addClass(classNames:String):JQuery = js.native
  def addClass(func:js.ThisFunction2[Element, Int, String, String]):JQuery = js.native
  
  /**
   * Insert content, specified by the parameter, after each element in the set of matched elements.
   */
  def after(content:ElementDesc*):JQuery = js.native
  def after(func:js.ThisFunction0[Element, ElementDesc]):JQuery = js.native
  def after(func:js.ThisFunction1[Element, Int, ElementDesc]):JQuery =  js.native
  
  
  /**
   * Insert content, specified by the parameter, to the end of each element in the set of matched elements.
   */
  def append(content:ElementDesc*):JQuery = js.native
  def append(func:js.ThisFunction2[Element, Int, String, js.Any]):JQuery = js.native

  /**
   * Insert every element in the set of matched elements to the end of the target.
   */
  def appendTo(target:ElementDesc):JQuery = js.native
  
  /**
   * Shorthand for get(x), lifted from scalajs-jquery
   */
  @js.annotation.JSBracketAccess
  def apply(x: Int): dom.html.Element = js.native
  
  /**
   * Get the value of an attribute for the first element in the set of matched elements.
   * 
   * Note that this returns UndefOr -- it is entirely legal for this to return undefined if
   * the attribute is not present, and that causes things to crash if it is not UndefOr.
   */
  def attr(attributeName:String):UndefOr[String] = js.native
  def attr(attributes:js.Dictionary[String]):JQuery = js.native
  /**
   * Set an attribute for the set of matched elements.
   */
  def attr(attributeName:String, v:AttrVal):JQuery = js.native
  def attr(attributeName:String, func:js.ThisFunction2[Element, Int, String, AttrVal]):JQuery = js.native
  
  /**
   * Insert content, specified by the parameter, before each element in the set of matched elements.
   */
  def before(content:ElementDesc, addlContent:ElementDesc*):JQuery = js.native
  def before(func:js.ThisFunction2[Element, Integer, String, String | Element | JQuery]):JQuery = js.native
  
  /**
   * Get the children of each element in the set of matched elements, optionally filtered by a selector.
   */
  def children(selector:String = ???):JQuery = js.native
  
  /**
   * Remove from the queue all items that have not yet been run.
   */
  def clearQueue(queueName:String = ???):JQuery = js.native

  /**
   * Create a deep copy of the set of matched elements.
   * 
   * Note that this requires an override because Scala.Object declares a clone() method which
   * is entirely unrelated.
   */
  override def clone():JQuery = js.native
  def clone(withDataAndEvents:Boolean):JQuery = js.native
  def clone(withDataAndEvents:Boolean, deepWithDataAndEvents:Boolean):JQuery = js.native
  
  /**
   * For each element in the set, get the first element that matches the selector 
   * by testing the element itself and traversing up through its ancestors in the DOM tree.
   */
  def closest(selector:String | Element | JQuery):JQuery = js.native
  def closest(selector:String, context:Element):JQuery = js.native
  
  /**
   * Get the children of each element in the set of matched elements, including text and comment nodes.
   */
  def contents():JQuery = js.native
  
  /**
   * Get the computed style properties for the first element in the set of matched elements.
   */
  def css(propertyName:String):String = js.native
  def css(propertyNames:Array[String]):js.Dictionary[String] = js.native
  def css(propertyName:String, value:String | Int):JQuery = js.native
  def css(properties:js.Dictionary[js.Any]):JQuery = js.native
  
  /**
   * Store arbitrary data associated with the matched elements.
   * 
   * undefined is not recognised as a data value. Calls such as .data( "name", undefined )
   * will return the corresponding data for "name", and is therefore the same as .data( "name" ).
   */
  def data(key: String, value: js.Any): JQuery = js.native
  def data(obj: js.Dictionary[js.Any]): JQuery = js.native
  /**
   * Return the value at the named data store for the first element in the jQuery collection, 
   * as set by data(name, value) or by an HTML5 data-* attribute. Undefined if that key is not set.
   */
  def data(key: String): UndefOr[js.Any] = js.native
  /**
   * Calling .data() with no parameters retrieves all of the values as a JavaScript object. 
   * This object can be safely cached in a variable as long as a new object is not set with .data(obj). 
   * Using the object directly to get or set values is faster than making individual calls to .data() 
   * to get or set each value.
   */
  def data(): js.Dictionary[js.Any] = js.native
  
  /**
   * Set a timer to delay execution of subsequent items in the queue.
   */
  def delay(duration:Int, queueName:String = ???):JQuery = js.native
  
  // NOTE: delegate() has been superceded by on().
  
  /**
   * Execute the next function on the queue for the matched elements.
   */
  def dequeue(queueName:String = ???):JQuery = js.native
  
  /**
   * Remove the set of matched elements from the DOM.
   */
  def detach():JQuery = js.native
  def detach(selector:String):JQuery = js.native
  
  /**
   * Iterate over a jQuery object, executing a function for each matched element.
   * 
   * Note that we do not bother with the full jQuery signature, since the "element" parameter
   * simply matches "this".
   * 
   * You can stop the loop from within the callback function by returning false. Otherwise, the return
   * value is irrelevant.
   */
  def each(func:js.ThisFunction0[Element, Any]):JQuery = js.native
  def each(func:js.ThisFunction1[Element, Int, Any]):JQuery = js.native
  
  /**
   * Remove all child nodes of the set of matched elements from the DOM.
   */
  def empty():JQuery = js.native
  
  /**
   * End the most recent filtering operation in the current chain and return the set of matched elements to its previous state.
   */
  def end():JQuery = js.native
  
  /**
   * Reduce the set of matched elements to the one at the specified index.
   */
  def eq(index:Integer):JQuery = js.native
 
  /**
   * Reduce the set of matched elements to those that match the selector or pass the function's test.
   */
  def filter(selector:Selector):JQuery = js.native
  def filter(func:js.ThisFunction0[Element, Boolean]):JQuery = js.native
  def filter(func:js.ThisFunction1[Element, Int, Boolean]):JQuery = js.native
  
  /**
   * Get the descendants of each element in the current set of matched elements, filtered by a selector, jQuery object, or element.
   */
  def find(selector:Selector):JQuery = js.native
  
  /**
   * Stop the currently-running animation, remove all queued animations, and complete all animations for the matched elements.
   */
  def finish(queue:String = "fx"):JQuery = js.native
  
  /**
   * Reduce the set of matched elements to the first in the set.
   */
  def first():JQuery = js.native
  
  /**
   * Retrieve one of the elements matched by the jQuery object.
   * 
   * If the value of index is out of bounds - less than the negative number of elements or equal to 
   * or greater than the number of elements - it returns undefined.
   */
  def get(index:Int):UndefOr[Element] = js.native
  /**
   * Retrieve the elements matched by the jQuery object.
   */
  def get():js.Array[_] = js.native
  
  /**
   * Reduce the set of matched elements to those that have a descendant that matches the selector or DOM element.
   */
  def has(selector:Selector):JQuery = js.native
  
  /**
   * Determine whether any of the matched elements are assigned the given class.
   */
  def hasClass(className:String):Boolean = js.native
  
  /**
   * Get the current computed height for the first element in the set of matched elements.
   */
  def height():Double = js.native
  /**
   * Set the CSS height of every matched element.
   */
  def height(value:Double | String):JQuery = js.native
  def height(value:js.ThisFunction2[Element, Integer, Integer, Number | String]):JQuery = js.native
  
  /**
   * Hide the matched elements.
   */
  def hide():JQuery = js.native
  def hide(duration:String | Int, complete:js.Function):JQuery = js.native
  def hide(duration:String | Int, easing:String = ???, complete:js.Function = ???):JQuery = js.native
  
  /**
   * Get the HTML contents of the first element in the set of matched elements.
   */
  def html():String = js.native
  /**
   * Set the HTML contents of every matched element.
   */
  def html(t:String):JQuery = js.native
  def html(func:js.ThisFunction2[Element, Int, String, String]):JQuery = js.native
  
  /**
   * Search for a given element from among the matched elements.
   */
  def index():Int = js.native
  def index(selector:ElementDesc):Int = js.native

  /**
   * Get the current computed inner height (including padding but not border) for the first element
   * in the set of matched elements.
   */
  def innerHeight():Double = js.native

  /**
   * Get the current computed inner width (including padding but not border) for the first element
   * in the set of matched elements.
   */
  def innerWidth():Double = js.native
  
  /**
   * Insert every element in the set of matched elements after the target.
   */
  def insertAfter(target:ElementDesc):JQuery = js.native
  
  /**
   * Insert every element in the set of matched elements before the target.
   */
  def insertBefore(target:ElementDesc):JQuery = js.native
  
  /**
   * Check the current matched set of elements against a selector, element,
   * or jQuery object and return true if at least one of these elements matches the given arguments.
   */
  def is(selector:Selector):Boolean = js.native
  /**
   * Note that this overload doesn't precisely match the jQuery documentation; we
   * elide the redundant Element param, since you have Element as the this parameter.
   */
  def is(func:js.ThisFunction1[Element, Int, Boolean]):Boolean = js.native
  
  /**
   * A string containing the jQuery version number.
   */
  def jquery:String = js.native
  
  /**
   * Reduce the set of matched elements to the final one in the set.
   */
  def last():JQuery = js.native
  
  /**
   * The number of elements in the jQuery object.
   */
  def length:Int = js.native

  /**
   * Pass each element in the current matched set through a function, producing a new jQuery object
   * containing the return values.
   * 
   * For Scala code, it is often more convenient to use the mapElems() extension function.
   * 
   * Within the callback function, this refers to the current DOM element for each iteration. The function
   * can return an individual data item or an array of data items to be inserted into the resulting set.
   * 
   * If a js.Array is returned, the elements inside the array are inserted into the set.
   * If the function returns null or undefined, no element will be inserted. (Note the implication: this
   * doesn't quite match the usual Scala semantics of map() -- there is a flatten component as well.) 
   */
  def map(func:js.ThisFunction0[Element, Any]):JQuery = js.native
  def map(func:js.ThisFunction1[Element, Int, Any]):JQuery = js.native
    
  /**
   * Get the immediately following sibling of each element in the set of matched elements. 
   * If a selector is provided, it retrieves the next sibling only if it matches that selector.
   */
  def next(selector:String = ???):JQuery = js.native
  
  /**
   * Get all following siblings of each element in the set of matched elements, optionally filtered by a selector.
   */
  def nextAll(selector:String = ???):JQuery = js.native
  
  /**
   * Get all following siblings of each element up to but not including the element matched by the selector, DOM node, or jQuery object passed.
   */
  def nextUntil():JQuery = js.native
  def nextUntil(selector:String):JQuery = js.native
  def nextUntil(selector:String, filter:String):JQuery = js.native
  def nextUntil(element:Element | JQuery, filter:String = ???):JQuery = js.native
  
  /**
   * Remove elements from the set of matched elements.
   */
  def not(selector:ElementDesc):JQuery = js.native
  def not(func:js.ThisFunction2[Element, Integer, Element, Boolean]):JQuery = js.native
  
  /**
   * Get the current coordinates of the first element in the set of matched elements, relative to the document.
   */
  def offset():JQueryPosition = js.native
  def offset(coordinates:JQueryPosition):JQuery = js.native
  def offset(func:js.Function2[Integer, JQueryPosition, JQueryPosition]):JQuery = js.native
  
  /**
   * Get the closest ancestor element that is positioned.
   */
  def offsetParent():JQuery = js.native
  
  /**
   * Get the current computed height for the first element in the set of matched elements, including
   * padding, border, and optionally margin.
   */
  def outerHeight():Double = js.native
  def outerHeight(includeMargin:Boolean):Double = js.native

  /**
   * Get the current computed width for the first element in the set of matched elements, including
   * padding, border, and optionally margin.
   */
  def outerWidth():Double = js.native
  def outerWidth(includeMargin:Boolean):Double = js.native

  /**
   * Get the parent of each element in the current set of matched elements, optionally filtered by a selector.
   * 
   * TBD: is the parameter really a Selector, or just a String? The JQuery API docs are unclear.
   */
  def parent(selector: String): JQuery = js.native
  def parent(): JQuery = js.native
  
  /**
   * Get the ancestors of each element in the current set of matched elements, optionally filtered by a selector.
   */
  def parents(selector:String):JQuery = js.native
  def parents():JQuery = js.native
  
  /**
   * Get the ancestors of each element in the current set of matched elements, up to but not 
   * including the element matched by the selector, DOM node, or jQuery object.
   */
  def parentsUntil():JQuery = js.native
  def parentsUntil(selector:String):JQuery = js.native
  def parentsUntil(selector:String, filter:String):JQuery = js.native
  def parentsUntil(element:Element | JQuery, filter:String = ???):JQuery = js.native
  
  /**
   * Get the current coordinates of the first element in the set of matched elements, relative to the offset parent.
   */
  def position():JQueryPosition = js.native
  
  /**
   * Insert content, specified by the parameters, to the beginning of each element in the set of matched elements.
   */
  def prepend(contents:ElementDesc*):JQuery = js.native
  def prepend(func:js.ThisFunction2[Element, Int, String, Selector]):JQuery = js.native
  
  /**
   * Insert every element in the set of matched elements to the beginning of the target.
   */
  def prependTo(target:ElementDesc):JQuery = js.native
  
  /**
   * Get the immediately preceding sibling of each element in the set of matched elements, optionally filtered by a selector.
   */
  def prev(selector:UndefOr[String] = js.undefined):JQuery = js.native
  
  /**
   * Get all preceding siblings of each element in the set of matched elements, optionally filtered by a selector.
   */
  def prevAll(selector:UndefOr[String] = js.undefined):JQuery = js.native
  
  /**
   * Get all preceding siblings of each element up to but not including the element matched by the selector, DOM node, or jQuery object.
   */
  def prevUntil():JQuery = js.native
  def prevUntil(selector:String):JQuery = js.native
  def prevUntil(selector:String, filter:String):JQuery = js.native
  def prevUntil(element:Element | JQuery, filter:String = ???):JQuery = js.native

  /**
   * Return a Promise object to observe when all actions of a certain type bound to the collection, queued or not, have finished.
   */
  def promise(tpe:String = ???, target:js.Object = ???):JQueryPromise = js.native
  
  /**
   * Get the value of a property for the first element in the set of matched elements.
   */
  def prop(propertyName:String):UndefOr[Any] = js.native
  /**
   * Set one or more properties for the set of matched elements.
   */
  def prop(propertyName:String, value:js.Any):JQuery = js.native
  def prop(properties:js.Dictionary[js.Any]):JQuery = js.native
  def prop(propertyName:String, func:js.ThisFunction2[Element, Int, Any, js.Any]):JQuery = js.native
  
  /**
   * Add a collection of DOM elements onto the jQuery stack.
   */
  def pushStack(elements:js.Array[Element]):JQuery = js.native
  def pushStack(elements:js.Array[Element], name:String, arguments:js.Array[js.Any]):JQuery = js.native
  
  /**
   * Show the queue of functions to be executed on the matched elements.
   */
  def queue(queueName:String = ???):js.Array[js.Function] = js.native
  def queue(newQueue:Array[js.Function]):JQuery = js.native
  def queue(queueName:String, newQueue:Array[js.Function]):JQuery = js.native
  def queue(callback:js.Function1[js.Function0[js.Any], Any]):JQuery = js.native
  
  /**
   * Specify a function to execute when the DOM is fully loaded.
   */
  def ready(handler:js.Function0[Any]):JQuery = js.native
  
  /**
   * Remove the set of matched elements from the DOM.
   */
  def remove():JQuery = js.native
  def remove(childSelector:String):JQuery = js.native
  
  /**
   * Remove an attribute from each element in the set of matched elements.
   */
  def removeAttr(attributeName:String):JQuery = js.native
  
  /**
   * Remove a single class, multiple classes, or all classes from each element in the set of matched elements.
   */
  def removeClass():JQuery = js.native
  def removeClass(classNames:String):JQuery = js.native
  def removeClass(func:js.ThisFunction2[Element, Int, String, String]):JQuery = js.native
  
  /**
   * Remove a previously-stored piece of data.
   */
  def removeData():JQuery = js.native
  def removeData(name:String):JQuery = js.native
  def removeData(list:js.Array[String]):JQuery = js.native
  
  /**
   * Remove a property for the set of matched elements.
   */
  def removeProp(propertyName:String):JQuery = js.native
  
  /**
   * Replace each target element with the set of matched elements.
   */
  def replaceAll(target:ElementDesc):JQuery = js.native
  
  /**
   * Replace each element in the set of matched elements with the provided new content and return the set of elements that was removed.
   */
  def replaceWith(content:ElementDesc):JQuery = js.native
  def replaceWith(func:js.ThisFunction0[Element, ElementDesc]):JQuery = js.native
  
  /**
   * Get the current horizontal position of the scroll bar for the first element in the set of matched elements.
   */
  def scrollLeft():Integer = js.native
  def scrollLeft(value:Double | Integer):JQuery = js.native

  /**
   * Get the current vertical position of the scroll bar for the first element in the set of
   * matched elements or set the vertical position of the scroll bar for every matched element.
   */
  def scrollTop():Double = js.native
  /**
   * Set the current vertical position of the scroll bar for each of the set of matched elements.
   * 
   * Note that this intentionally takes Double -- while you usually want to set it to an Int, there
   * are occasions when being able to take a Double (that is, a full JS Number) is convenient in code.
   */
  def scrollTop(value:Double | Integer):JQuery = js.native
  
  /**
   * Encode a set of form elements as a string for submission.
   */
  def serialize():String = js.native
  
  /**
   * Encode a set of form elements as an array of names and values.
   */
  def serializeArray():Array[JQuerySerializeArrayElement] = js.native
  
  /**
   * Hide the matched elements.
   */
  def show():JQuery = js.native
  def show(duration:String | Int, complete:js.Function):JQuery = js.native
  def show(duration:String | Int, easing:String = ???, complete:js.Function = ???):JQuery = js.native

  /**
   * Get the siblings of each element in the set of matched elements, optionally filtered by a selector.
   */
  def siblings(selector:String = ???):JQuery = js.native
  
  /**
   * Reduce the set of matched elements to a subset specified by a range of indices.
   */
  def slice(start:Integer, end:Integer = ???):JQuery = js.native
  

  /**
   * Get the combined text contents of each element in the set of matched elements, including their descendants.
   */
  def text():String = js.native
  /**
   * Set the content of each element in the set of matched elements to the specified text.
   */
  def text(t:String):JQuery = js.native
  // TBD: the JQ docs don't say that this is a ThisFunction. Is it? Probably, based on html()?
  def text(func:js.Function2[Int, String, String]):JQuery = js.native
  
  /**
   * Retrieve all the elements contained in the jQuery set, as an array.
   */
  def toArray():js.Array[Element] = js.native
  
  /**
   * Add or remove one or more classes from each element in the set of matched elements, 
   * depending on either the class's presence or the value of the state argument.
   */
  def toggleClass():JQuery = js.native
  def toggleClass(className:String, state:Boolean = ???):JQuery = js.native
  def toggleClass(state:Boolean):JQuery = js.native
  def toggleClass(func:js.Function3[Integer, String, Boolean, String]):JQuery = js.native
  def toggleClass(func:js.Function3[Integer, String, Boolean, String], state:Boolean):JQuery = js.native
  
  /**
   * Remove the parents of the set of matched elements from the DOM, leaving the matched elements in their place.
   */
  def unwrap():JQuery = js.native
  
  /**
   * Shorthand modifier, lifted from scalajs-jquery.
   */
  @js.annotation.JSBracketAccess
  def update(x: Int, v: dom.html.Element): Unit = js.native
  
  /**
   * Get the value of this JQuery.
   * 
   * "value" is highly context-dependent. The signature is loose because it can return a
   * String, a Number (?) or an Array, depending on circumstances. See the extension methods
   * in JQueryExtensions for more strongly-typed versions that you can use when you expect
   * a specific return type.
   */
  def `val`(): js.Dynamic = js.native
  def `val`(value: js.Array[String]): JQuery = js.native
  def `val`(value: String): JQuery = js.native
  def `val`(func: js.Function2[Int, String, String]): JQuery = js.native
  @JSName("val") def value(): js.Dynamic = js.native
  @JSName("val") def value(value: js.Array[String]): JQuery = js.native
  @JSName("val") def value(value: String): JQuery = js.native
  @JSName("val") def value(func: js.Function2[Int, String, String]): JQuery = js.native
  
  
  /**
   * Get the current computed width for the first element in the set of matched elements.
   */
  def width():Double = js.native
  /**
   * Set the CSS width of every matched element.
   */
  def width(value:Double | String):JQuery = js.native
  def width(value:js.ThisFunction2[Element, Integer, Integer, Number | String]):JQuery = js.native
  
  /**
   * Wrap an HTML structure around each element in the set of matched elements.
   */
  def wrap(wrappingElement:String | Element | JQuery):JQuery = js.native
  def wrap(func:js.ThisFunction1[Element, Integer, String | JQuery]):JQuery = js.native
  
  /**
   * Wrap an HTML structure around all elements in the set of matched elements.
   */
  def wrapAll(wrappingElement:String | Element | JQuery):JQuery = js.native
  def wrapAll(func:js.ThisFunction1[Element, Integer, String | JQuery]):JQuery = js.native
  
  /**
   * Wrap an HTML structure around the content of each element in the set of matched elements.
   */
  def wrapInner(wrappingElement:String | Element | JQuery):JQuery = js.native
  def wrapInner(func:js.ThisFunction1[Element, Integer, String | JQuery]):JQuery = js.native
}

/**
 * Returned by offset() and position().
 * 
 * Note that the values in here are intentionally not Integers. From the JQuery docs:
 * 
 * "The number returned by dimensions-related APIs, including .offset(), may be fractional in some
 *  cases. Code should not assume it is an integer. Also, dimensions may be incorrect when the page
 *  is zoomed by the user; browsers do not expose an API to detect this condition."
 */
@js.native
trait JQueryPosition extends js.Object {
  val left:Double
  val top:Double
}

/**
 * Returned by serializeArray().
 */
@js.native
trait JQuerySerializeArrayElement extends js.Object {
  def name:String = js.native
  def value:String = js.native
}
