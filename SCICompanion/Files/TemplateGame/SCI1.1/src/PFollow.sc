;;; Sierra Script 1.0 - (do not remove this comment)
(script# PATHFOLLOW_SRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use PolyPath)


;	
;	 A motion class that is used to make an Actor stay within a certain distance of another (possibly moving) object, while avoiding polygon obstacles.
;	 This is similar to :class:`PChase`, except that the Actor doesn't stop moving when it reaches the distance target.
;	
;	 PFollow is generally used as an argument for setMotion::
;	
;	 	; Make the dog follow the ego at a distance of 20
;	 	(aDog setMotion: PFollow gEgo 20)
(class PFollow of PolyPath
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
	; .. function:: init(theClient theWho [theDistance theObstacles])	
	;
	; 	Initializes the PFollow instance.
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param heapPtr theWho: The target to follow.
	; 	:param number theDistance: How close the client needs to get from the target.
	; 	:param heapPtr theObstacles: Optional list of polygon obstacles. If not provided, the room's obstacles are used.
	;
	(method (init theClient theWho theDistance theObstacles &tmp temp0)
		(if argc
			(= temp0
				(if (>= argc 4) theObstacles else (gRoom obstacles?))
			)
			(if (>= argc 1)
				(= client theClient)
				(if (>= argc 2)
					(= who theWho)
					(= targetX (who x?))
					(= targetY (who y?))
					(if (>= argc 3) (= distance theDistance))
				)
			)
			(super init: client targetX targetY 0 1 temp0)
		else
			(super init:)
		)
	)
	
	(method (doit &tmp temp0 temp1)
		(if
			(>
				(GetDistance targetX targetY (who x?) (who y?))
				distance
			)
			(if points (Memory memFREE points))
			(= points 0)
			(= value 2)
			(self init: client who)
			0
		else
			(= temp0 (client distanceTo: who))
			(if (<= temp0 distance)
				(= temp1
					(GetAngle (client x?) (client y?) (who x?) (who y?))
				)
				(if (!= (client heading?) temp1)
					(client setHeading: temp1)
				)
				(= xLast (client x?))
				(= yLast (client y?))
				(= b-moveCnt gGameTime)
				0
			else
				(super doit:)
			)
		)
	)
	
	(method (moveDone)
		(self setTarget: init:)
	)
)
