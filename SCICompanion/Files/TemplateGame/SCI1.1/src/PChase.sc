;;; Sierra Script 1.0 - (do not remove this comment)
(script# 930)
(include sci.sh)
(use Main)
(use PolyPath)
(use System)


;	
;	 A motion class that is used to make an Actor get within a certain distance of another (possibly moving) object, while avoiding polygon obstacles.
;	
;	 PChase is generally used as an argument for setMotion::
;	
;	 	; Make the peasant approach the merchant, within 20 pixels.
;	 	(peasant setMotion: PChase theMerchant 20)
(class PChase of PolyPath
	(properties
		client 0
		caller 0
		x 0
		y 0
		dx 0
		dy 0
		b-moveCnt 0
		b-i1 0
		b-i2 0
		b-di 0
		b-xAxis 0
		b-incr 0
		completed 0
		xLast 0
		yLast 0
		value 2
		points 0
		finalX 0
		finalY 0
		obstacles 0
		who 0
		distance 0
		targetX 0
		targetY 0
	)
	
	;
	; .. function:: init(theClient theWho [theDistance theCaller theObstacles])
	;
	; 	Initializes the PFollow instance.
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param heapPtr theWho: The target to follow.
	; 	:param number theDistance: How close the client needs to get from the target.
	; 	:param heapPtr theCaller: The object on which cue() will be called when the target is reached.
	; 	:param heapPtr theObstacles: Optional list of polygon obstacles. If not provided, the room's obstacles are used.
	;
	(method (init theClient theWho theDistance theCaller theObstacles &tmp [temp0 20])
		(if argc
			(cond 
				((>= argc 5) (= obstacles theObstacles))
				((not (IsObject obstacles)) (= obstacles (gRoom obstacles?)))
			)
			(if (>= argc 1)
				(= client theClient)
				(if (>= argc 2)
					(= who theWho)
					(= targetX (who x?))
					(= targetY (who y?))
					(if (>= argc 3)
						(= distance theDistance)
						(if (>= argc 4) (= caller theCaller))
					)
				)
			)
			(super init: client targetX targetY caller 1 obstacles)
		else
			(super init:)
		)
	)
	
	(method (doit &tmp temp0)
		(if
			(>
				(GetDistance targetX targetY (who x?) (who y?))
				distance
			)
			(if points (Memory memFREE points))
			(= points 0)
			(= value 2)
			(self init: client who)
		else
			(= temp0 (client distanceTo: who))
			(if (<= temp0 distance)
				(self moveDone:)
			else
				(super doit:)
			)
		)
	)
	
	(method (moveDone &tmp temp0 [temp1 20])
		(= temp0 (client distanceTo: who))
		(cond 
			((<= temp0 distance) (super moveDone:))
			((== (WordAt points value) $7777)
				(if points (Memory memFREE points))
				(= points 0)
				(= value 2)
				(self init: client who)
			)
			(else (self setTarget: init:))
		)
	)
)
