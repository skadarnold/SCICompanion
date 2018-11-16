## `&exists`
`&exists` serves to make the processing of optional arguments clearer.
```lisp
(method (posn theX theY theZ)
	(if (>= argc 1)
		(= x theX)
		(if (>= argc 2)
			(= y theY)
			(if (>= argc 3) (= z theZ))
		)
	)
	(BaseSetter self)
	(self forceUpd:)
)
```
```lisp
(method (posn theX theY theZ)
	(if (&exists theX)
		(= x theX)
		(if (&exists theY)
			(= y theY)
			(if (&exists theZ)
				(= z theZ)
			)
		)
	)
	(BaseSetter self)
	(self forceUpd:)
)
```

## `LDM`/`STM` opcodes
Why write cumbersome `Memory` kernel calls when you can use `*` and get proper pointers?
```lisp
; Given a local array...
(procedure (DerefTest &tmp ptr)
	(= ptr @numbers) ; Take the address of the numbers array.
	(Printf "*ptr is %d" *ptr) ; Print the value for what is effectively [numbers 0]
	(= *ptr 6) ; Same as (= [numbers 0] 6) but faster
	(Printf "*ptr is %d" *ptr)
	(= ptr (+ ptr 2)) ; All values are 16 bits.
	(= *ptr 7) ; Same as (= [numbers 1] 7)
	(Printf "*ptr is %d" *ptr)
)
```

## `foreach`
```lisp
; No need to predeclare n, a &tmp variable is made for you.
(foreach n anArray
	; (do something with n)
)

; Works on anything based on the List kernel calls.
(foreach item aCollection
	; (do something with item)
)

; With LDM/STM enabled and a predefined iterator
(foreach &n anArray
	; (do something with n)
)
```
Iterating over a collection is different from how Phil's `foreach` does it so watch out.

## `verbs`
Another one that's quite distinct from Phil's, the `verbs` block expands into a standard SCI11 `doVerb` method.
```lisp
(instance anExample of Prop
	(verbs
		(V_TALK
			(++ exampleTalks)
			(StartConversation 124)
		)
		(V_DO (gRoom setScript: TouchExampleScript))
		(V_ANITEM (Print "You get the idea."))
	)
)
```
Once expanded, an `else` handler to call the superclass' `doVerb` is thrown in for free.

But wait. Phil's experimental branch has no `verbs`. It has `nearVerbs`, `farVerbs`, and `invVerbs`! That's
true, and that's tailored towards the interpreter used for *Cascadia Quest* and such. This is more generically
appropriate for SCI11.

("Where's the second parameter" you might ask. I checked the leaked SCI16 template, and there's no such
parameter to be found. I checked the SCI Companion SCI11 template, and it didn't *use* `param2`, just blindly
passed it to `super`. I did find a use in *Police Quest 3* though, but that's not SCI11.)

## `&getpoly`
SCI Companion's polygon editor is great, but the way the data it produces is used results in a major code smell.
Polygons take local variable space, and there are three methods to process them for use. The `&getpoly` keyword
lets you produce polygon-loading bytecode that's nigh-indistinguishable from what you'd see if you decompile a
random Sierra game (assuming that game is new enough to *use* polygons).

To use `&getpoly`, give each individual polygon a distinct name (the blank string counts) and *remove the
`(include ___.shp)` line* from your room's script. Next, replace the `(AddPolygonsToRoom)` call in your room's
`init` with something like this:
```lisp
(gRoom addObstacle: (&getpoly "room"))
```
You can pass as many `(&getpoly "...")` calls to `addObstacle` at once as you'd like. Once all your rooms have
been converted at your leisure, you can safely remove `AddPolygonsToRoom` and its support procedures from your
*Main* script.
