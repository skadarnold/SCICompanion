;;; Sierra Script 1.0 - (do not remove this comment)
(script# 988)
(include sci.sh)
(use Main)
(use PolyPath)
(use Cycle)
(use Actor)


;	
;	 The Ego represents the player's main character. It extends :class:`Actor` by handling the following:
;	
;	 - Sends the player to a new room when they walk off screen.
;	 - Moves the player to a new destination when the user clicks the walk icon somewhere.
;	 - Handles getting and losing inventory items.
(class Ego of Actor
	(properties
		x 0
		y 0
		z 0
		heading 0
		noun 0
		_case 0
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
		signal $2000
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
		illegalBits $8000
		xLast 0
		yLast 0
		xStep 3
		origStep 770
		moveSpeed 6
		blocks 0
		baseSetter 0
		mover 0
		looper 0
		viewer 0
		avoider 0
		code 0
		edgeHit 0
	)
	
	(method (init)
		(super init:)
		(if (not cycler) (self setCycle: Walk))
	)
	
	(method (doit)
		(super doit:)
		(= edgeHit
			(cond 
				((<= x gEdgeDistance) 4)
				((>= x (- 319 gEdgeDistance)) 2)
				((>= y (- 189 (/ gEdgeDistance 2))) 3)
				((<= y (gRoom horizon?)) 1)
				(else 0)
			)
		)
	)
	
	; Handles moving the ego around.
	(method (handleEvent pEvent &tmp temp0 temp1 temp2)
		(= temp1 (pEvent type?))
		(= temp2 (pEvent message?))
		(cond 
			((and script (script handleEvent: pEvent)) 1)
			((& temp1 $0040)
				(= temp0 temp2)
				(if (and (== temp0 0) (& temp1 $0004))
					(pEvent claimed?)
					(return)
				)
				(if
					(and
						(& temp1 $0004)
						(== temp0 (gUser prevDir?))
						(IsObject mover)
					)
					(= temp0 0)
				)
				(gUser prevDir: temp0)
				(self setDirection: temp0)
				(pEvent claimed: TRUE)
			)
			((& temp1 evVERB)
				(if (& temp1 evMOVE)
					(switch gEgoUseObstacles
						(0
							(self setMotion: MoveTo (pEvent x?) (+ (pEvent y?) z))
						)
						(1
							(self
								setMotion: PolyPath (pEvent x?) (+ (pEvent y?) z)
							)
						)
						(2
							(self
								setMotion: PolyPath (pEvent x?) (+ (pEvent y?) z) 0 0
							)
						)
					)
					(gUser prevDir: 0)
					(pEvent claimed: TRUE)
				else
					(super handleEvent: pEvent)
				)
			)
			(else (super handleEvent: pEvent))
		)
		(pEvent claimed?)
	)
	
	; Always returns TRUE. You **are** me!
	(method (facingMe)
		(return TRUE)
	)
	
	;
	; .. function:: get(invNumber [...])
	;
	; 	Gives the ego an inventory item (or items).
	;
	; 	:param number invNumber: The numerical identifier of the inventory item.
	;
	; 	Example usage::
	;
	; 		; Give the ego the crystal and the lizard tail.
	; 		( gEgo get: INV_CRYSTAL INV_LIZARDTAIL)
	;
	(method (get invNumbers &tmp temp0)
		(= temp0 0)
		(while (< temp0 argc)
			((gInv at: [invNumbers temp0]) moveTo: self)
			(++ temp0)
		)
	)
	
	;
	; .. function:: put(invNumber [newOwner])
	;
	; 	Removes an inventory item from the ego, and optionally gives it to a new owner.
	;
	; 	:param number invNumber: The numerical identifier of the inventory item.
	; 	:param number newOwner: The optional new owner (typically a room number).
	;
	; 	Example usage::
	;
	; 		; The ego used lizard tail, so remove it from the inventory:
	; 		(gEgo put: INV_LIZARDTAIL)
	;
	(method (put invNumber newOwner &tmp temp0)
		(if (self has: invNumber)
			(= temp0 (gInv at: invNumber))
			(temp0 moveTo: (if (== argc 1) -1 else newOwner))
			(if
			(and gIconBar (== (gIconBar curInvIcon?) temp0))
				(gIconBar
					curInvIcon: 0
					disable: ((gIconBar useIconItem?) cursor: 999 yourself:)
				)
			)
		)
	)
	
	;	
	;	 Determines if the ego has an inventory item.
	;	
	;	 :param number invNumber: The numerical identifier of the inventory item.
	;	
	;	 Example usage::
	;	
	;	 	(if (gEgo has: INV_LIZARDTAIL)
	;	 		; Do something...
	;	 	)
	;	
	(method (has invNumber &tmp temp0)
		(= temp0 (gInv at: invNumber))
		(if temp0
			((= temp0 (gInv at: invNumber)) ownedBy: self)
		)
	)
)
