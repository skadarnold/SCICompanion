;;; Sierra Script 1.0 - (do not remove this comment)
(script# DOOR_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use PolyPath)
(use Polygon)
(use Sound)
(use Cycle)
(use Actor)


;	
;	 Door offers support for automatically managing the open and close state of the door, and optionally
;	 offering access to another room.
;	
;	 Example definition::
;	
;	 	(instance frontDoor of Door
;	 		(properties
;	 			x 39
;	 			y 167
;	 			noun N_FRONTDOOR
;	 			approachX 36
;	 			approachY 175
;	 			view 210
;	 			entranceTo 230
;	 			moveToX 22
;	 			moveToY 169
;	 			enterType 0
;	 			exitType 0
;	 		)
;	 	)
(class Door of Prop
	(properties
		x 0
		y 0
		z 0
		heading 0
		noun 0
		modNum -1
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		sightAngle $6789
		actions 0
		onMeCheck omcDISABLE
		state $0000
		approachX 0
		approachY 0
		approachDist 0
		_approachVerbs 0
		yStep 2
		view -1
		loop 0
		cel 0
		priority 0
		underBits 0
		signal $0000
		lsTop 0
		lsLeft 0
		lsBottom 0
		lsRight 0
		brTop 0
		brLeft 0
		brBottom 0
		brRight 0
		scaleSignal $0000
		scaleX 128
		scaleY 128
		maxScale 128
		cycleSpeed 6
		script 0
		cycler 0
		timer 0
		detailLevel 0
		scaler 0
		entranceTo 0            ; Entrance to which room?
		locked 0                ; Is the door locked?
		lockedCase 0            ; The message condition to use when displaying a "locked" message.
		openSnd 0               ; Open sound number.
		closeSnd 0              ; Close sound number.
		openVerb 0              ; The verb to use for opening and closing the door.
		listenVerb 0            ; The verb to use for listening.
		doubleDoor 0
		forceOpen 0
		forceClose 1
		caller 0
		moveToX 0
		moveToY 0
		enterType 2
		exitType 2
		closeScript 0
		openScript 0
		doorPoly 0
		polyAdjust 5            ; Amount to inflate base rect when creating a polygon.
	)
	
	(method (init)
		(self approachVerbs: openVerb listenVerb)
		(if
			(or
				forceOpen
				(and
					gPreviousRoomNumber
					(== gPreviousRoomNumber entranceTo)
				)
			)
			(= state 2)
		)
		(super init:)
		(self createPoly:)
		(self ignoreActors: 1)
		(if (== state 0)
			(= cel 0)
			(if doubleDoor (doubleDoor setCel: 0))
		else
			(gAltPolyList delete: doorPoly)
			(= cel (- (NumCels self) 1))
			(if doubleDoor
				(doubleDoor setCel: (- (NumCels doubleDoor) 1))
			)
		)
		(if (== state 2)
			(if closeScript
				(self setScript: closeScript)
			else
				(switch enterType
					(0
						(gEgo
							posn: moveToX moveToY
							setMotion: PolyPath approachX approachY self
						)
					)
					(1
						(gEgo
							edgeHit: 0
							posn: approachX approachY
							setHeading: heading
						)
						(if forceClose (self close:))
					)
					(2
						(if forceClose (self close:))
					)
				)
			)
		else
			(self stopUpd:)
		)
	)
	
	(method (dispose)
		(gAltPolyList delete: doorPoly)
		(doorPoly dispose:)
		(super dispose:)
	)
	
	(method (doVerb theVerb)
		(switch theVerb
			(openVerb
				(if (== state 2) (self close:) else (self open:))
			)
			(listenVerb (self listen:))
			(else  (super doVerb: theVerb))
		)
	)
	
	(method (cue)
		(switch state
			(3
				(= state 0)
				(self stopUpd:)
				(gAltPolyList add: doorPoly)
				(if caller (caller cue:))
				(if (not (gUser controls?)) (gGame handsOn: 1))
			)
			(1
				(= state 2)
				(self stopUpd:)
				(gAltPolyList delete: doorPoly)
				(if caller (caller cue:))
				(if openScript
					(self setScript: openScript)
				else
					(switch exitType
						(0
							(if (or moveToX moveToY)
								(gEgo
									illegalBits: 0
									setMotion: PolyPath moveToX moveToY self
								)
							)
						)
						(1
							(if (or moveToX moveToY)
								(gEgo setMotion: PolyPath moveToX moveToY self)
							)
						)
						(2
							(if (not (gUser controls?)) (gGame handsOn: 1))
						)
					)
				)
			)
			(else 
				(cond 
					(
					(and (== (gEgo x?) moveToX) (== (gEgo y?) moveToY))
						(cond 
							(entranceTo
								(switch entranceTo
									((gRoom north?)
										(gEgo edgeHit: 1)
									)
									((gRoom south?)
										(gEgo edgeHit: 3)
									)
									((gRoom west?)
										(gEgo edgeHit: 4)
									)
									((gRoom east?)
										(gEgo edgeHit: 2)
									)
								)
								(gRoom newRoom: entranceTo)
							)
							(forceClose (self close:))
							(caller (caller cue:))
						)
					)
					(
						(and
							(== (gEgo x?) approachX)
							(== (gEgo y?) approachY)
						)
						(cond 
							(forceClose (self close:))
							(caller (caller cue:))
						)
					)
				)
			)
		)
	)
	
	; Opens the door if it's not locked.
	(method (open)
		(if locked
			(if (== modNum -1) (= modNum gRoomNumber))
			(gMessager say: noun 0 lockedCase 0 0 modNum)
		else
			(if (gUser controls?) (gGame handsOff:))
			(= state 1)
			(self setCycle: EndLoop self)
			(if openSnd (doorSound number: openSnd play:))
			(if doubleDoor (doubleDoor setCycle: EndLoop))
		)
	)
	
	; Closes the door.
	(method (close)
		(= state 3)
		(self setCycle: BegLoop self)
		(if closeSnd (doorSound number: closeSnd play:))
		(if doubleDoor (doubleDoor setCycle: BegLoop))
	)
	
	; Override this method in your door instance to provide logic for listening.
	(method (listen)
	)
	
	;
	; .. function:: createPoly([polyPoints])
	;
	; 	Adds a :class:`Polygon` for the door to the gAltPolyList.
	;
	; 	:param number polyPoints: A list of points for the polygon. If none are provided, the Door's base rect is used.
	;
	(method (createPoly)
		(= doorPoly
			((Polygon new:) type: PBarredAccess yourself:)
		)
		(if argc
			(doorPoly init: &rest)
		else
			(doorPoly
				init:
					(- brLeft polyAdjust)
					(+ brBottom polyAdjust)
					(- brLeft polyAdjust)
					(- brTop polyAdjust)
					(+ brRight polyAdjust)
					(- brTop polyAdjust)
					(+ brRight polyAdjust)
					(+ brBottom polyAdjust)
			)
		)
		(gAltPolyList add: doorPoly)
	)
)

(instance doorSound of Sound
	(properties
		flags $0001
	)
)
