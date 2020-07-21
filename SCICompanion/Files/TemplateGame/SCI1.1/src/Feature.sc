;;; Sierra Script 1.0 - (do not remove this comment)
(script# 950)
(include sci.sh)
(use Main)
(use PolyPath)
(use System)


;	
;	 This is a static Script object that works in conjunction with :class:`Feature` when making the
;	 ego move close to the Feature in response to interacting with the Feature. When the ego is in range, the
;	 action is then repeated.
;	
;	 Generally, you shouldn't need to bother with this class.
(class CueObj of Script
	(properties
		client 0
		state $ffff
		start 0
		timer 0
		cycles 0
		seconds 0
		lastSeconds 0
		ticks 0
		lastTicks 0
		register 0
		script 0
		caller 0
		next 0
		theVerb 0
	)
	
	(method (changeState newState)
		(switch (= state newState)
			(1
				(gEgo
					setHeading:
						(GetAngle (gEgo x?) (gEgo y?) (client x?) (client y?))
						self
				)
				(gTheDoits add: self)
			)
			(2 (= cycles 3))
			(3
				(gTheDoits delete: self)
				(if
					(not
						(if
						(and (IsObject client) (IsObject (client actions?)))
							((client actions?) doVerb: theVerb)
						)
					)
					(client doVerb: theVerb)
				)
				(= state 0)
			)
		)
	)
)

;	
;	 Feature forms the base class for interactable items in the game. Actor, Prop and View all extend from this class.
;	 It can also be used on its own, generally for attaching interactions to static portions of the background.
;	
;	 Feature has support for displaying messages when the player performs actions (verbs) by clicking on it. It will
;	 also optionally direct the ego to approach the feature.
;	
;	 Example usage::
;	
;	 	; Define a red plant that displays messages associated with the noun N_PLANT
;	 	; when the use clicks on the white control color on the pic background.
;	 	; The (x, y) coordinates are used to identify the "center of interest" of the feature,
;	 	; which determines the direction in which the ego should face when interacting with it.
;	 	(instance redPlant of Feature
;	 		(properties
;	 			x 217
;	 			y 121
;	 			noun N_PLANT
;	 			onMeCheck ctlWHITE
;	 		)
;	 	)
;	
;	 There are two main ways to identify the bounds of a feature: control colors, and polygons. Even if onMeCheck
;	 has been set in the Feature declaration, this needs to be done with the setOnMeCheck() method.
;	
;	 You can also indicate for which verbs the ego should approach the feature (as opposed to
;	 just facing it). This is done with the approachVerbs() method.
;	
;	 Example initialization::
;	
;	 	(redPlanet
;	 		approachVerbs: V_DO V_LOOK
;	 		setOnMeCheck: omcCOLORS ctlWHITE
;	 		init:
;	 	)
(class Feature of Object
	(properties
		x 0                     ; "center of interest" for the Feature.
		y 0
		z 0
		heading 0
		noun 0                  ; Message noun associated with the Feature.
		_case 0                 ; Message case associated with the Feature.
		modNum -1
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE    ; omcDISABLE, omcCOLORS or omcPOLYGON. See setOnMeCheck().
		state $0000
		approachX 0             ; The approach spot x.
		approachY 0             ; The approach spot y.
		approachDist 0          ; The approach distance.
		_approachVerbs 0        ; Bitmask for verbs that cause the ego to approach the Feature.
	)
	
	;
	; .. function:: init([initCode])
	;
	; 	Initializes the Feature. This must be done prior to it being active in the scene.
	;
	; 	:param heapPtr initCode: Optional initialization :class:`Code` for the feature. Generally, this is left unspecified.
	;
	(method (init initCode)
		(self initialize: (if argc initCode else 0))
		(if (self respondsTo: #underBits)
			(gCast add: self)
		else
			(gFeatures add: self)
		)
	)
	
	(method (dispose)
		(if actions (actions dispose:) (= actions 0))
		(if
		(and (IsObject onMeCheck) (not (& state $0004)))
			(onMeCheck dispose:)
			(= onMeCheck 0)
		)
		(gFeatures delete: self)
		(if (& state $0001) (Memory memFREE name) (= name 0))
		(super dispose:)
	)
	
	(method (initialize param1)
		(cond 
			((and argc param1) (self perform: param1))
			(gFeatureInit (self perform: gFeatureInit))
		)
	)
	
	(method (handleEvent pEvent)
		(cond 
			((pEvent claimed?) (return 1))
			(
				(and
					(& (pEvent type?) evVERB)
					(self onMe: pEvent)
					(self isNotHidden:)
				)
				(CueObj
					state: 0
					cycles: 0
					client: self
					theVerb: (pEvent message?)
				)
				(pEvent claimed: TRUE)
				(if
					(and
						(gUser canControl:)
						(& (gEgo state?) $0002)
						(>
							(GetDistance (gEgo x?) (gEgo y?) approachX approachY)
							approachDist
						)
						gApproachCode
						(&
							_approachVerbs
							(gApproachCode doit: (pEvent message?))
						)
					)
					(gEgo
						setMotion: PolyPath approachX (+ (gEgo z?) approachY) CueObj
					)
				else
					(gEgo setMotion: 0)
					(if (self facingMe:) (CueObj changeState: 3))
				)
			)
		)
		(return (pEvent claimed?))
	)
	
	;	
	;	 This method can be overridden by Feature instances to provide custom logic when the
	;	 player interacts with the feature. By default, it displays any message resource associated
	;	 with the given verb for this Feature's noun.
	;	
	(method (doVerb theVerb &tmp temp0 temp1)
		(= temp0 (if gDoVerbCode else dftDoVerb))
		(if (== modNum -1) (= modNum gRoomNumber))
		(if
			(and
				gMessageType
				(Message msgGET modNum noun theVerb _case 1)
			)
			(gMessager say: noun theVerb _case 0 0 modNum)
		else
			(temp0 doit: theVerb self)
		)
	)
	
	(method (notFacing &tmp temp0)
		(gEgo setMotion: 0)
		(CueObj client: self state: 0 cycles: 0 cue:)
	)
	
	;
	; .. function:: facingMe([theObj])
	;
	; 	:param heapPtr theObj: Optional object that the Feature might be facing. If not specified, the player's ego is used.
	;
	; 	Returns TRUE if this Feature is facing the specified object (or ego).
	;
	(method (facingMe theTheGEgo &tmp theGEgo temp1)
		(cond 
			(argc (= theGEgo theTheGEgo))
			((gCast contains: gEgo) (= theGEgo gEgo))
			(else (return 1))
		)
		(= temp1
			(Abs
				(-
					(GetAngle (theGEgo x?) (theGEgo y?) x y)
					(theGEgo heading?)
				)
			)
		)
		(if (> temp1 180) (= temp1 (- 360 temp1)))
		(return
			(if (<= temp1 sightAngle)
				(return 1)
			else
				(if (!= sightAngle $6789) (self notFacing:))
				(return 0)
			)
		)
	)
	
	(method (isNotHidden)
		(return 1)
	)
	
	;
	; .. function:: onMe(theObj)
	;
	; .. function:: onMe(x y)
	;
	; 	:param heapPtr theObj: The object that might be on the Feature.
	; 	:param number x: The x coordinate.
	; 	:param number y: The y coordinate.
	; 	:returns: TRUE if the (x, y) or object is on the Feature.
	;
	(method (onMe param1 param2 &tmp temp0 temp1)
		(if (IsObject param1)
			(= temp0 (param1 x?))
			(= temp1 (param1 y?))
		else
			(= temp0 param1)
			(= temp1 param2)
		)
		(return
			(cond 
				((& state $0004)
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
						(& onMeCheck (OnControl 4 temp0 temp1))
					else
						0
					)
				)
				((IsObject onMeCheck) (AvoidPath temp0 temp1 onMeCheck))
				(
					(or
						(not (if (or nsLeft nsRight nsTop) else nsBottom))
						(and
							(<= nsLeft temp0)
							(<= temp0 nsRight)
							(<= nsTop temp1)
							(<= temp1 nsBottom)
						)
					)
					1
				)
				(else 0)
			)
		)
	)
	
	;
	; .. function:: approachVerbs([verbs ...])
	;
	; 	Indicates which verbs should cause the ego to approach the feature.
	;
	; 	:param number verbs: An optional series of verbs.
	;
	; 	Example usage::
	;
	; 		(theToy approachVerbs: V_DO V_EAT)
	;
	(method (approachVerbs param1 &tmp temp0 temp1)
		(= _approachVerbs 0)
		(if (and argc gApproachCode [param1 0])
			(= temp0 0)
			(while (< temp0 argc)
				(= temp1 (gApproachCode doit: [param1 temp0]))
				(self _approachVerbs: (| (self _approachVerbs?) temp1))
				(++ temp0)
			)
		)
	)
	
	(method (setName theName)
		(= state (| state $0001))
		(= name (Memory memALLOC_CRIT (+ (StrLen theName) 1)))
		(StrCpy name theName)
	)
	
	;
	; .. function:: setOnMeCheck(omcDISABLE)
	;
	; .. function:: setOnMeCheck(omcPOLYGON polygon)
	;
	; .. function:: setOnMeCheck(omcCOLORS [colors ...])
	;
	; 	Controls how the Feature is identified (colors or polygon), or disables the onMe check.
	;
	; 	:param heapPtr polygon: A polygon defining the boundaries of the Feature
	; 	:param number colors: Control color(s) defining the Feature.
	;
	; 	Example usage::
	;
	; 		(theTable setOnMeCheck: omcCOLORS ctlWHITE ctlGREY)
	;
	; 		(theRock setOnMeCheck: omcPOLYGON (&getpoly "rock"))
	;
	(method (setOnMeCheck checkStyle params &tmp temp0)
		(switch checkStyle
			(omcDISABLE
				(= onMeCheck checkStyle)
				(= state (& state $fffb))
			)
			(omcPOLYGON
				(= onMeCheck [params 0])
				(= state (& state $fffb))
			)
			(omcCOLORS
				(= temp0 (= onMeCheck 0))
				(while (< temp0 (- argc 1))
					(= onMeCheck (| onMeCheck [params temp0]))
					(++ temp0)
				)
				(= state (| state $0004))
			)
		)
	)
)

(instance dftDoVerb of Code
	(properties)
	
	(method (doit)
		(return 1)
	)
)
