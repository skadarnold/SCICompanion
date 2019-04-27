;;; Sierra Script 1.0 - (do not remove this comment)
;	
;	 The Actor script contains classes to handle views, actors, props, and any other sprite in your game.
(script# 998)
(include sci.sh)
(use Main)
(use Print)
(use PolyPath)
(use Feature)
(use Cycle)
(use System)


;	
;	 The View class is an essential part of SCI games. It is the base class for :class:`Prop`, :class:`Actor` and the like.
;	 It extends :class:`Feature` by providing the ability to be dynamically positioned at different places, and by automatically setting
;	 its bounds based on its view, loop and cel. It is often (though not exclusively) used to add static views to the background, via
;	 its addToPic method.
;	
;	 Example definition::
;	
;	 	(instance ship of View
;	 		(properties
;	 			x 6
;	 			y 92
;	 			noun N_SHIP
;	 			view 113
;	 			loop 2
;	 			cel 1
;	 			signal ignAct	; Don't interact with Actors
;	 		)
;	 	)
;	
;	 Example initialization::
;	
;	 	(ship init:)
;	
(class View of Feature
	(properties
		x 0                 ; x position. See posn().
		y 0                 ; y position. See posn().
		z 0                 ; z position. See posn().
		heading 0           ; The angle direction the View faces.
		noun 0              ; The noun for the View (for messages).
		_case 0             ; The optional case for the View (for messages).
		modNum -1           ; Module number (for messages)
		nsTop 0             ; "Now seen" rect. The visual bounds of the View.
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE    ; The type of onMe checks that are done.
		state $0000
		approachX 0         ; The approach spot x.
		approachY 0         ; The approach spot y.
		approachDist 0      ; The approach distance.
		_approachVerbs 0    ; Bitmask indicating which verbs cause the ego to approach.
		yStep 2
		view -1             ; The view number for View.
		loop 0              ; The loop of the View.
		cel 0               ; The cel of the View.
		priority 0          ; The priority of the View.
		underBits 0
		signal $0101
		lsTop 0             ; The "last seen" rect.
		lsLeft 0
		lsBottom 0
		lsRight 0
		brTop 0             ; The "base rect".
		brLeft 0
		brBottom 0
		brRight 0
		scaleSignal $0000
		scaleX 128          ; Current x scale.
		scaleY 128          ; Current y scale.
		maxScale 128        ; Max scale.
	)
	
	(method (init &tmp temp0)
		(= temp0 (if (& signal $0020) gAddToPics else gCast))
		(= signal (& signal $7fff))
		(if (not (temp0 contains: self))
			(= lsRight (= lsBottom (= lsLeft (= lsTop 0))))
			(= signal (& signal $ff77))
		)
		(BaseSetter self)
		(temp0 add: self)
		(if (== temp0 gAddToPics)
			(if (not (& signal fixPriOn))
				(= priority (CoordPri y))
			)
			(SetNowSeen self)
			(temp0 doit:)
		)
		(self initialize: checkDetail:)
	)
	
	(method (dispose)
		(self startUpd: hide:)
		(= signal (| signal $8000))
	)
	
	; Prints a description of the View on the screen.
	(method (showSelf)
		(Print addText: name addIcon: view loop cel init:)
	)
	
	(method (isNotHidden)
		(return (not (& signal $0088)))
	)
	
	;
	; .. function:: onMe(x y)
	;
	; .. function:: onMe(obj)
	;
	; 	Determines if an object or an (x, y) coordinate lies within the View.
	;
	; 	:param heapPtr obj: An object with x and y properties.
	; 	:returns: TRUE if it's on the View, otherwise FALSE.
	;
	(method (onMe param1 param2 &tmp temp0 temp1)
		(if (IsObject param1)
			(= temp0 (param1 x?))
			(= temp1 (param1 y?))
		else
			(= temp0 param1)
			(= temp1 param2)
		)
		(cond 
			((& signal $0080) 0)
			(
			(and (not (IsObject onMeCheck)) (& signal skipCheck))
				(if
					(or
						(not (if (or nsLeft nsRight nsTop) else nsBottom))
						(and
							(<= nsLeft temp0)
							(<= temp0 nsRight)
							(<= nsTop temp1)
							(<= temp1 nsBottom)
						)
					)
					(not
						(IsItSkip
							view
							loop
							cel
							(- temp1 nsTop)
							(- temp0 nsLeft)
						)
					)
				)
			)
			(else (super onMe: temp0 temp1))
		)
	)
	
	;
	; .. function:: posn(theX theY [theZ])
	;
	; 	Sets the position of the View and updates its base rectangle.
	;
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
	
	(method (stopUpd)
		(= signal (| signal notUpd))
		(= signal (& signal $fffd))
	)
	
	(method (forceUpd)
		(= signal (| signal $0040))
	)
	
	(method (startUpd)
		(= signal (| signal $0002))
		(= signal (& signal $fffe))
	)
	
	;	
	;	 :param number thePriority: The new priority of the View. If -1 is specified, the View updates its priority based on its y coordinate.
	;	
	(method (setPri thePriority)
		(cond 
			((== argc 0) (= signal (| signal fixPriOn)))
			((== thePriority -1) (= signal (& signal $ffef)))
			(else (= priority thePriority) (= signal (| signal fixPriOn)))
		)
		(self forceUpd:)
	)
	
	; Sets the loop of the View.
	(method (setLoop theLoop)
		(cond 
			((== argc 0) (= signal (| signal noTurn)))
			((== theLoop -1) (= signal (& signal $f7ff)))
			(else (= loop theLoop) (= signal (| signal noTurn)))
		)
		(self forceUpd:)
	)
	
	; Sets the cel of the View.
	(method (setCel theCel)
		(cond 
			((== argc 0) 0)
			((== theCel -1) 0)
			(else
				(= cel
					(if (>= theCel (self lastCel:))
						(self lastCel:)
					else
						theCel
					)
				)
			)
		)
		(self forceUpd:)
	)
	
	;	
	;	 Tells the View if it should or should not interact with (bump into) other objects.
	;	
	;	 :param boolean shouldIgnore: If TRUE (or not specified), the View should ignore other objects.
	;	
	(method (ignoreActors shouldIgnore)
		(if (or (== 0 argc) shouldIgnore)
			(= signal (| signal ignAct))
		else
			(= signal (& signal $bfff))
		)
	)
	
	; Hides the View.
	(method (hide)
		(= signal (| signal $0008))
	)
	
	; Shows the View.
	(method (show)
		(= signal (& signal $fff7))
	)
	
	(method (delete)
		(if (& signal $8000)
			(= signal (& signal $7fff))
			(cond 
				((gAddToPics contains: self) (gAddToPics delete: self) (= signal (& signal $ffdf)))
				((& signal $0020) (gCast delete: self) (gAddToPics add: self) (return))
				(else (gCast delete: self))
			)
			(if underBits (UnLoad 133 underBits) (= underBits 0))
			(super dispose:)
			(if (IsObject actions) (actions dispose:))
			(= actions 0)
		)
	)
	
	; Draws the View permanently on the background.
	(method (addToPic)
		(if (gCast contains: self)
			(= signal (| signal $8021))
		else
			(= signal (| signal $0020))
			(self init:)
		)
	)
	
	; Returns the last valid cel of the View.
	(method (lastCel)
		(return (- (NumCels self) 1))
	)
	
	(method (motionCue)
	)
	
	(method (checkDetail)
	)
	
	;
	; .. function:: setScale([theY])
	;
	; 	Sets the scale of the View. If no parameters are provided, the view
	; 	will be scaling, but not have auto-scaling.
	;
	; 	:param number theY: The y parameter corresponding to 100% size for auto-scaling. Passing 0 will disable scaling.
	;
	(method (setScale theY &tmp temp0 temp1 temp2 [temp3 40])
		(cond 
			((not argc)
				(= scaleSignal (| scaleSignal ssScalable))
				(= scaleSignal (& scaleSignal (~ ssAutoScale)))
			)
			((not theY)
				(= scaleSignal
					(& scaleSignal (~ (| ssScalable ssAutoScale)))
				)
			)
			((< theY (gRoom vanishingY?))
				(Printf
					{<%s setScale:> y value less than vanishingY}
					name
				)
			)
			(else
				(= temp0 (- theY (gRoom vanishingY?)))
				(= temp1 (- 190 theY))
				(= temp2 (+ (/ (* temp1 100) temp0) 100))
				(= scaleSignal (| scaleSignal ssScalable ssAutoScale))
				(= maxScale (/ (* temp2 128) 100))
			)
		)
	)
)

;	
;	 Prop extends :class:`View` by providing the following additional abilities:
;	
;	 	- cycling through animation frames
;	 	- attach Scripts
;	 	- attach a Scaler
;	
;	 Example definition::
;	
;	 	(instance monitor of Prop
;	 		(properties
;	 			x 13
;	 			y 161
;	 			noun N_MONITOR
;	 			view 1142
;	 			loop 1
;	 			priority 15
;	 			signal fixPriOn
;	 			cycleSpeed 14
;	 		)
;	 	)
;	
;	 Example initialization::
;	
;	 	(monitor:
;	 		init:
;	 		setCycle: Forward
;	 		setScript: sPlayMC
;	 	)
(class Prop of View
	(properties
		x 0                             ; x position. See posn().
		y 0                             ; y position. See posn().
		z 0                             ; z position. See posn().
		heading 0                       ; The angle direction the Prop faces.
		noun 0                          ; The noun for the Prop (for messages).
		_case 0                         ; The optional case for the Prop (for messages).
		modNum -1                       ; Module number (for messages)
		nsTop 0                         ; "Now seen" rect. The visual bounds of the Prop.
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE            ; The type of onMe checks that are done.
		state $0000
		approachX 0                     ; The approach spot x.
		approachY 0                     ; The approach spot y.
		approachDist 0                  ; The approach distance.
		_approachVerbs 0                ; Bitmask indicating which verbs cause the ego to approach.
		yStep 2
		view -1                         ; The view for Prop.
		loop 0                          ; Loop for the Prop.
		cel 0                           ; Current cel of the Prop.
		priority 0                      ; Priority screen value of the Prop.
		underBits 0
		signal $0000
		lsTop 0                         ; The "last seen" rect...
		lsLeft 0
		lsBottom 0
		lsRight 0
		brTop 0                         ; The "base rect", used for collison detection.
		brLeft 0
		brBottom 0
		brRight 0
		scaleSignal $0000
		scaleX 128                      ; Current x scale.
		scaleY 128                      ; Current y scale.
		maxScale 128                    ; Max scale.
		cycleSpeed 6                    ; How quickly the Prop animation cycles.
		script 0                        ; Arbitrary :class:`Script` object.
		cycler 0                        ; :class:`Cycle` attached to the Prop.
		timer 0
		detailLevel 0
		scaler 0                        ; :class:`Scaler` object attached to the Prop.
	)
	
	(method (doit &tmp temp0)
		(if (& signal $8000) (return))
		(if script (script doit:))
		(if (and (& signal $0004) (not (& signal $0002)))
			(return)
		)
		(if cycler (cycler doit:))
		(if scaler (scaler doit:))
	)
	
	; Lets the Prop's script have a chance at handling the event.
	(method (handleEvent pEvent)
		(if script (script handleEvent: pEvent))
		(super handleEvent: pEvent)
	)
	
	(method (delete)
		(if (& signal $8000)
			(self setScript: 0 setCycle: 0)
			(if timer (timer dispose:))
			(if (IsObject scaler) (scaler dispose:) (= scaler 0))
			(super delete:)
		)
	)
	
	(method (motionCue)
		(if (and cycler (cycler completed?))
			(cycler motionCue:)
		)
	)
	
	(method (checkDetail param1)
		(cond 
			((not detailLevel))
			(
				(<
					(if argc param1 else (gGame detailLevel:))
					detailLevel
				)
				(self stopUpd:)
			)
			(cycler (self startUpd:))
		)
	)
	
	;
	; .. function:: setScale(class params)
	;
	; .. function:: setScale(obj params)
	;
	; .. function:: setScale(-1 otherObj)
	;
	; .. function:: setScale(scale)
	;
	; 	Provides various ways to control the scaling of a Prop. See :class:`Scaler` and :class:`ScaleTo`.
	;
	; 	Example usage for attaching a dynamic scaler::
	;
	; 		(gEgo setScale: Scaler frontSize backSize frontY backY)
	;
	; 	Example usage for setting an explicit scale::
	;
	; 		(gEgo setScale: 50) ; 50 percent size.
	;
	; 	:param class class: A scaler class, such as Scaler.
	; 	:param heapPtr obj: An instance of a scaler class.
	; 	:param params: Initialization parameters for the scaler.
	; 	:param heapPtr otherObj: Another object from which to copy scaling information.
	; 	:param number scale: A percentage scale.
	;
	(method (setScale param1 param2)
		(if scaler (scaler dispose:) (= scaler 0))
		(cond 
			((not argc) (super setScale:))
			((IsObject param1)
				(= scaleSignal (| scaleSignal ssScalable))
				(= scaleSignal (& scaleSignal (~ ssAutoScale)))
				(= scaler
					(if (& (param1 -info-?) $8000)
						(param1 new:)
					else
						param1
					)
				)
				(scaler init: self param2 &rest)
			)
			((== param1 -1)
				(if (param2 scaleSignal?)
					(= scaleSignal (param2 scaleSignal?))
					(= maxScale (param2 maxScale?))
					(if (IsObject (param2 scaler?))
						((= scaler ((param2 scaler?) new:)) client: self)
					)
				)
			)
			(else (super setScale: param1))
		)
	)
	
	;	
	;	 Sets a cycler object on the Prop, optionally supplying initialization parameters for the cycler.
	;	
	;	 Example usage for telling a Prop to cycle forward::
	;	
	;	 	(theFrog setCycle: Forward)
	;	
	;	 Example usage for telling a Prop to cycle to the end, then cue its caller::
	;	
	;	 	(theFrog setCycle: EndLoop self)
	;	
	;	 :param class theCycler: A class derived from :class:`Cycle`, or NULL to remove the current cycler.
	;	
	;	
	(method (setCycle theCycler)
		(if cycler (cycler dispose:))
		(if theCycler
			(self startUpd:)
			(= cycler
				(if (& (theCycler -info-?) $8000)
					(theCycler new:)
				else
					theCycler
				)
			)
			(cycler init: self &rest)
		else
			(= cycler 0)
		)
	)
	
	;	
	;	 Attaches an arbitrary script to the Prop, optionally providing initialization parameters.
	;	
	;	 Example usage::
	;	
	;	 	(self setScript: theRoomScript)
	;	
	;	
	(method (setScript theScript)
		(if (IsObject script) (script dispose:))
		(if theScript (theScript init: self &rest))
	)
	
	(method (cue)
		(if script (script cue:))
	)
)

;	
;	 Actor is the base class for moving objects in your game. It extends :class:`Prop` by providing the following additional capabilities:
;	
;	 	- A mover property that is responsible for controlling how the Actor moves. This is assigned with setMotion().
;	 	- An optional Avoider that makes the Actor avoid objects.
;	 	- Optional "blocks" that indicate areas the Actor can or can't be.
;	
;	 Example definition::
;	
;	 	(instance wd40 of Actor
;	 		(properties
;	 			x 20
;	 			y 20
;	 			noun N_ROBOT
;	 			view 400
;	 			loop 8
;	 			signal ignAct
;	 		)
;	 	)
;	
;	 Example initialization::
;	
;	 	(wd40
;	 		init:
;	 		setMotion: PolyPath 127 128
;	 	)
;	
(class Actor of Prop
	(properties
		x 0                     ; x position. See posn().
		y 0                     ; y position. See posn().
		z 0                     ; z position. See posn().
		heading 0               ; The angle direction the Actor faces.
		noun 0                  ; The noun for the Actor (for messages).
		_case 0                 ; The optional case for the Actor (for messages).
		modNum -1               ; Module number (for messages)
		nsTop 0                 ; "Now seen" rect. The visual bounds of the Actor.
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE    ; The type of onMe checks that are done.
		state $0000
		approachX 0             ; The approach spot x.
		approachY 0             ; The approach spot y.
		approachDist 0          ; The approach distance.
		_approachVerbs 0        ; Bitmask indicating which verbs cause the ego to approach.
		yStep 2                 ; The number of pixels moved in the y direction each cycle.
		view -1                 ; The view for Actor.
		loop 0
		cel 0
		priority 0
		underBits 0
		signal $0000
		lsTop 0                 ; The "last seen" rect...
		lsLeft 0
		lsBottom 0
		lsRight 0
		brTop 0                 ; The "bounds rect" (near the feet of the Actor).
		brLeft 0
		brBottom 0
		brRight 0
		scaleSignal $0000
		scaleX 128              ; Current x scale.
		scaleY 128              ; Current y scale.
		maxScale 128            ; Max scale.
		cycleSpeed 6            ; How quickly the Actor animation cycles.
		script 0                ; Arbitrary :class:`Script` object.
		cycler 0                ; :class:`Cycle` attached to the Actor.
		timer 0
		detailLevel 0
		scaler 0                ; :class:`Scaler` object attached to the Actor.
		illegalBits $8000
		xLast 0
		yLast 0
		xStep 3
		origStep 770
		moveSpeed 6             ; How quickly the Actor moves.
		blocks 0
		baseSetter 0
		mover 0                 ; The :class:`Motion` object attached to the Actor.
		looper 0                ; Optional looper code.
		viewer 0
		avoider 0
		code 0
	)
	
	(method (init)
		(super init: &rest)
		(= xLast x)
		(= yLast y)
	)
	
	(method (doit &tmp temp0 theBrLeft theBrRight temp3 temp4 temp5 temp6 temp7)
		(if (& signal $8000) (return))
		(if script (script doit:))
		(if code (code doit: self))
		(if (and (& signal $0004) (not (& signal $0002)))
			(return)
		)
		(if viewer (viewer doit: self))
		(if avoider (avoider doit:))
		(if mover
			(if
				(and
					(& scaleSignal ssScalable)
					(not (& scaleSignal ssNotStepScale))
				)
				(= temp5 (>> origStep $0008))
				(= temp6 (& origStep $00ff))
				(= temp7 (/ (* temp5 scaleX) 128))
				(= temp3 (if temp7 else 1))
				(= temp7 (/ (* temp6 scaleY) 128))
				(= temp4 (if temp7 else 1))
				(if (or (!= temp3 xStep) (!= temp4 yStep))
					(self setStep: temp3 temp4 1)
				)
			)
			(if mover (mover doit:))
		)
		(if scaler (scaler doit:))
		(if cycler
			(= theBrLeft brLeft)
			(= theBrRight brRight)
			(cycler doit:)
			(if baseSetter
				(baseSetter doit: self)
			else
				(BaseSetter self)
			)
		)
		(= xLast x)
		(= yLast y)
	)
	
	;
	; .. function:: posn(theX theY [theZ])
	;
	; 	Sets the position of the Actor.
	;
	(method (posn theX theY)
		(super posn: theX theY &rest)
		(= xLast theX)
		(= yLast theY)
	)
	
	;
	; .. function:: setLoop(loop)
	;
	; .. function:: setLoop(loopClass [params ...])
	;
	; .. function:: setLoop(looper [params ...])
	;
	; 	Sets a loop on the Actor, or sets an object that controls which loop is used.
	;
	; 	:param number loop: A loop number.
	; 	:param class loopClass: A class that has a doit method that controls its client loop (e.g. see :class:`Grooper`).
	; 	:param heapPtr looper: An instance of a looper class.
	;
	(method (setLoop param1 &tmp theLooper)
		(= theLooper
			(cond 
				((== argc 0) (super setLoop:) 0)
				((not (IsObject param1)) (super setLoop: param1 &rest) 0)
				((& (param1 -info-?) $8000) (param1 new:))
				(else param1)
			)
		)
		(if theLooper
			(if looper (looper dispose:))
			((= looper theLooper) init: self &rest)
		)
	)
	
	(method (delete)
		(if (& signal $8000)
			(if (!= mover -1) (self setMotion: 0))
			(self setAvoider: 0)
			(if baseSetter (baseSetter dispose:) (= baseSetter 0))
			(if looper (looper dispose:) (= looper 0))
			(if viewer (viewer dispose:) (= viewer 0))
			(if blocks (blocks dispose:) (= blocks 0))
			(if code (code dispose:) (= code 0))
			(if (IsObject actions)
				(actions dispose:)
				(= actions 0)
			)
			(super delete:)
		)
	)
	
	(method (motionCue)
		(if (and mover (mover completed?)) (mover motionCue:))
		(super motionCue:)
	)
	
	(method (checkDetail param1)
		(cond 
			((not detailLevel))
			(
				(<
					(if argc param1 else (gGame detailLevel:))
					detailLevel
				)
				(self stopUpd:)
			)
			((or cycler mover) (self startUpd:))
		)
	)
	
	;	
	;	 Assigns a mover object to the Actor. The mover is initialized with the Actor and any
	;	 sendParams that have been provided.
	;	
	;	 :param theMover: A class name, or an instance that inherits from :class:`Motion`.
	;	 :param sendParams: Any params that should be forwarded to the mover's init() method.
	;	
	;	 Movers control the Actor's motion, whether it be via
	;	 mouse or keyboard input, or some in-game logic.
	;	
	;	 Example usage for moving a ball to a particular position, and cueing the caller when it's done::
	;	
	;	 	(myBall setMotion: MoveTo 123 100 self)
	;	
	(method (setMotion theMover)
		(if (and mover (!= mover -1)) (mover dispose:))
		(if theMover
			(self startUpd:)
			(= mover
				(if (& (theMover -info-?) $8000)
					(theMover new:)
				else
					theMover
				)
			)
			(mover init: self &rest)
		else
			(= mover 0)
		)
	)
	
	(method (setAvoider theAvoider)
		(if avoider (avoider dispose:))
		(= avoider
			(if
				(and
					(IsObject theAvoider)
					(& (theAvoider -info-?) $8000)
				)
				(theAvoider new:)
			else
				theAvoider
			)
		)
		(if avoider (avoider init: self &rest))
	)
	
	(method (ignoreHorizon param1)
		(if (or (not argc) param1)
			(= signal (| signal ignoreHorizon))
		else
			(= signal (& signal $dfff))
		)
	)
	
	;	
	;	 Specifies the control colors which the Actor's movement. This is not used commonly in SCI1.1. Constraining
	;	 an Actor's motion is generally done with Polygons instead.
	;	
	(method (observeControl bits &tmp temp0)
		(= temp0 0)
		(while (< temp0 argc)
			(= illegalBits (| illegalBits [bits temp0]))
			(++ temp0)
		)
	)
	
	;	
	;	 Specifies which control colors should no longer block the Actor's movement.
	;	
	(method (ignoreControl bits &tmp temp0)
		(= temp0 0)
		(while (< temp0 argc)
			(= illegalBits (& illegalBits (~ [bits temp0])))
			(++ temp0)
		)
	)
	
	;	
	;	 Adds a block (an instance which inherits from :class:`Blk`) to the Actor's list of blocks.
	;	 These control where an Actor is allowed to go. In SCI1.1, these have generally been replaced by Polygons.
	;	
	(method (observeBlocks)
		(if (not blocks) (= blocks (Set new:)))
		(blocks add: &rest)
	)
	
	; Removes a block from the Actor's list of blocks.
	(method (ignoreBlocks)
		(if blocks
			(blocks delete: &rest)
			(if (blocks isEmpty:) (blocks dispose:) (= blocks 0))
		)
	)
	
	; Returns TRUE if the Actor is not moving, FALSE otherwise.
	(method (isStopped)
		(if (IsObject mover)
			(if
			(and (== x (mover xLast?)) (== y (mover yLast?)))
				(return TRUE)
			)
			(return FALSE)
		)
		(return TRUE)
	)
	
	(method (isBlocked)
		(return (& signal $0400))
	)
	
	; Returns TRUE if the Actor is inside the specified rectangle, FALSE otherwise.
	(method (inRect left top right bottom)
		(return
			(if (and (<= left x) (<= x right) (<= top y))
				(<= y bottom)
			else
				0
			)
		)
	)
	
	;
	; .. function:: onControl([fUsePoint])
	;
	; 	Provides a bitmask of the control colors on which an Actor is located.
	;
	; 	:param boolean fUsePoint: If TRUE, the Actor's location is used. If FALSE (or not specified), the Actor's base rectangle (near its feet) is used.
	; 	:returns: A bitmask of ctl flags. These should usually be tested with the & operator.
	;
	; 	Example usage::
	;
	; 		(if (& ctlGREEN (gEgo onControl:))
	; 			(Prints {The ego is on Green})
	; 		)
	;
	(method (onControl fUsePoint)
		(if (and argc fUsePoint)
			(OnControl 4 x y)
		else
			(OnControl 4 brLeft brTop brRight brBottom)
		)
	)
	
	;	
	;	 :param heapPtr obj: An object with x and y properties.
	;	 :returns: the distance between this Actor and the object.
	;	
	(method (distanceTo obj)
		(GetDistance x y (obj x?) (obj y?) gPicAngle)
	)
	
	; Returns TRUE if the Actor can't be in its current location.
	(method (cantBeHere &tmp temp0)
		(if baseSetter
			(baseSetter doit: self)
		else
			(BaseSetter self)
		)
		(= temp0
			(cond 
				((CantBeHere self (gCast elements?)))
				(
					(and
						(not (& signal ignoreHorizon))
						(IsObject gRoom)
						(< y (gRoom horizon?))
					)
					-1
				)
				(
				(and blocks (not (blocks allTrue: #doit self))) -2)
			)
		)
	)
	
	;
	; .. function:: setStep(newX newY [fDontSetOrigStep])
	;
	; 	Sets the pixel increments in which the Actor moves. Bigger increments means the Actor will cover
	; 	larger distances in each frame.
	;
	; 	:param number newX: The xStep, or -1 if not provided.
	; 	:param number newY: The yStep, or -1 if not provided.
	; 	:param boolean fDontSetOrigStep: Optional flag telling us not to set origStep.
	;
	(method (setStep newX newY fDontSetOrigStep &tmp theXStep theYStep)
		(= theXStep (>> origStep $0008))
		(= theYStep (& origStep $00ff))
		(if (and (>= argc 1) (!= newX -1)) (= theXStep newX))
		(if (and (>= argc 2) (!= newY -1)) (= theYStep newY))
		(if (or (< argc 3) (not fDontSetOrigStep))
			(= origStep (+ (<< theXStep $0008) theYStep))
		)
		(= xStep theXStep)
		(= yStep theYStep)
		(if
			(and
				(IsObject mover)
				(or
					(mover isMemberOf: MoveTo)
					(mover isMemberOf: PolyPath)
				)
			)
			(mover init:)
		)
	)
	
	;	
	;	 Sets the direction that the Actor faces.
	;	
	;	 :param number newDirection: One of CENTER, UP, UPRIGHT, RIGHT, DOWNRIGHT, DOWN, DOWNLEFT, LEFT or UPLEFT.
	;	
	(method (setDirection newDirection &tmp temp0 temp1 temp2 temp3 temp4 temp5 temp6 temp7)
		(= temp1 (gRoom vanishingY?))
		(= temp0
			(if (== temp1 -30000) x else (gRoom vanishingX?))
		)
		(if (and (== xStep 0) (== yStep 0)) (return))
		(= temp5 (/ 32000 (Max xStep yStep)))
		(switch newDirection
			(0 (self setMotion: 0) (return))
			(1
				(= temp2 (- temp0 x))
				(= temp3 (- temp1 y))
			)
			(5
				(= temp2 (- x temp0))
				(= temp3 (- y temp1))
			)
			(3 (= temp2 temp5) (= temp3 0))
			(7
				(= temp2 (- temp5))
				(= temp3 0)
			)
			(else 
				(= temp4 (GetAngle x y temp0 temp1))
				(if (< 180 temp4) (= temp4 (- temp4 360)))
				(= temp4
					(+ (/ (+ temp4 90) 2) (* 45 (- newDirection 2)))
				)
				(= temp2 (SinMult temp4 100))
				(= temp3 (- (CosMult temp4 100)))
			)
		)
		(= temp5 (/ temp5 5))
		(while
		(and (< (Abs temp3) temp5) (< (Abs temp2) temp5))
			(= temp2 (* temp2 5))
			(= temp3 (* temp3 5))
		)
		(= temp7 (gRoom obstacles?))
		(if (and temp7 gEgoUseObstacles)
			(= temp6
				(AvoidPath
					x
					y
					(+ x temp2)
					(+ y temp3)
					(temp7 elements?)
					(temp7 size?)
					0
				)
			)
			(= temp2 (- (WordAt temp6 2) x))
			(= temp3 (- (WordAt temp6 3) y))
			(Memory memFREE temp6)
		)
		(cond 
			((or temp2 temp3) (self setMotion: MoveTo (+ x temp2) (+ y temp3)))
			(newDirection
				(self
					setMotion: 0
					setHeading: (* (- newDirection 1) 45)
				)
			)
			(else (self setMotion: 0))
		)
	)
	
	; Sets the angle heading of the Actor.
	(method (setHeading theHeading cueObj)
		(if argc (= heading theHeading))
		(if looper
			(looper
				doit: self heading (if (>= argc 2) cueObj else 0)
			)
		else
			(DirLoop self heading)
			(if (and (>= argc 2) (IsObject cueObj))
				(cueObj cue: &rest)
			)
		)
		(return heading)
	)
	
	; Sets the speed of the Actor. This controls both the move and cycle speed.
	(method (setSpeed newSpeed)
		(if argc (= moveSpeed (= cycleSpeed newSpeed)))
		(return moveSpeed)
	)
)
