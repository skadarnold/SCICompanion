;;; Sierra Script 1.0 - (do not remove this comment)
(script# 945)
(include sci.sh)
(use Main)
(use Cycle)
(use System)


;	
;	 A Motion class that is used to move an Actor to a destination while following a path that avoids polygon obstacles. This is usually what is used
;	 to move the ego when the player clicks on a destination. See the init method for more details on parameters.
;	
;	 Example usage::
;	
;	 	; Make the thief move to (255, 146), then cue the current object when done.
;	 	(theThief setMotion: PolyPath 255 146 self)
(class PolyPath of Motion
	(properties
		client 0            ; The :class:`Actor` to which this is attached.
		caller 0            ; The object that will get cue()'d when the motion is complete.
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
	)
	
	;
	; .. function:: init(theClient theFinalX theFinalY [theCaller optimizationLevel theObstacles])
	;
	; 	Initializes the PolyPath instance.
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param number theFinalX: The target x coordinate.
	; 	:param number theFinalY: The target y coordinate.
	; 	:param heapPtr theCaller: The object on which cue() will be called when the target is reached.
	; 	:param number optimizationLevel: This appears to be unused.
	; 	:param heapPtr theObstacles: Optional list of polygon obstacles. If not provided, the room's obstacles are used.
	;
	(method (init theClient theFinalX theFinalY theCaller optimizationLevel theObstacles &tmp [temp0 30])
		(if argc
			(= client theClient)
			(if (> argc 1)
				(cond 
					((>= argc 6) (= obstacles theObstacles))
					((not (IsObject obstacles)) (= obstacles (gRoom obstacles?)))
				)
				(if points (Memory memFREE points))
				(= points
					(AvoidPath
						(theClient x?)
						(theClient y?)
						(= finalX theFinalX)
						(= finalY theFinalY)
						(if obstacles (obstacles elements?) else 0)
						(if obstacles (obstacles size?) else 0)
						(if (>= argc 5) optimizationLevel else 1)
					)
				)
				(if (> argc 3) (= caller theCaller))
			)
			(self setTarget:)
		)
		(super init:)
	)
	
	(method (dispose)
		(if points (Memory memFREE points))
		(= points 0)
		(super dispose:)
	)
	
	(method (moveDone)
		(if (== (WordAt points value) $7777)
			(super moveDone:)
		else
			(self setTarget: init:)
		)
	)
	
	(method (setTarget &tmp pathResult theX theY polyCount [temp4 30])
		(if (!= (WordAt points value) $7777)
			(= x (WordAt points value))
			(= y (WordAt points (++ value)))
			(++ value)
			(if
				(and
					(IsObject gAltPolyList)
					(= polyCount (gAltPolyList size?))
				)
				(= pathResult
					(AvoidPath
						(client x?)
						(client y?)
						x
						y
						(gAltPolyList elements?)
						polyCount
						0
					)
				)
				(= theX (WordAt pathResult 2))
				(= theY (WordAt pathResult 3))
				(if (or (!= x theX) (!= y theY))
					(= x theX)
					(= y theY)
					(Memory memPOKE (+ points value 2) $7777)
				)
				(Memory memFREE pathResult)
			)
		)
	)
)
