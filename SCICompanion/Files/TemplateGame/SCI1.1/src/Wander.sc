;;; Sierra Script 1.0 - (do not remove this comment)
(script# 970)
(include sci.sh)
(use Cycle)


;	
;	 The Wander mover makes an Actor wander around aimlessly.
;	
;	 Example usage::
;	
;	 	; Make the man wander, up to a distance of 20 pixels from each place he stops.
;	 	(theMan setMotion: Wander 20)
;	
;	 Note that this class won't intelligently direct an Actor around polygon obstacles.
(class Wander of Motion
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
		distance 30
	)
	
	;
	; .. function:: init(theClient theDistance)
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param number theDistance: The maximum distance to wander each time.
	;
	(method (init theClient theDistance)
		(if (>= argc 1)
			(= client theClient)
			(if (>= argc 2) (= distance theDistance))
		)
		(self setTarget:)
		(super init: client)
	)
	
	(method (doit)
		(super doit:)
		(if (client isStopped:) (self moveDone:))
	)
	
	(method (moveDone)
		(self init:)
	)
	
	(method (setTarget &tmp temp0)
		(= temp0 (* distance 2))
		(= x (+ (client x?) (- distance (Random 0 temp0))))
		(= y (+ (client y?) (- distance (Random 0 temp0))))
	)
	
	(method (onTarget)
		(return 0)
	)
)
