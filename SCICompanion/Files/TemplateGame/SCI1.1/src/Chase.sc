;;; Sierra Script 1.0 - (do not remove this comment)
(script# 972)
(include sci.sh)
(use Cycle)


;	
;	 A motion class that is used to make an Actor move until it is within a certain distance of another (possibly moving) object. Then it stops.
;	
;	 Example usage::	
;	
;	 	; Make the dog get within 20 pixels of the ego.
;	 	(aDog setMotion: Chase gEgo 20)
;	
;	 Note that this class doesn't intelligently move the Actor around polygon obstacles. For that, use :class:`PChase`.
(class Chase of Motion
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
		who 0
		distance 0
	)
	
	;
	; .. function:: init(theClient theWho [theDistance theCaller])
	;
	; 	Initializes the Chase instance.
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param heapPtr theWho: The target to chase.
	; 	:param number theDistance: How close the client needs to get from the target.
	; 	:param heapPtr theCaller: The object on which cue() will be called when the target is reached.
	;
	(method (init theClient theWho theDistance theCaller)
		(if (>= argc 1)
			(= client theClient)
			(if (>= argc 2)
				(= who theWho)
				(if (>= argc 3)
					(= distance theDistance)
					(if (>= argc 4) (= caller theCaller))
				)
			)
		)
		(super init: client (who x?) (who y?) caller)
	)
	
	(method (doit)
		(if (self onTarget:)
			(self moveDone:)
		else
			(super doit:)
			(if (== b-moveCnt 0)
				(super init: client (who x?) (who y?) caller)
			)
		)
	)
	
	(method (setTarget)
		(cond 
			(argc (super setTarget: &rest))
			((not (self onTarget:)) (super setTarget: (who x?) (who y?)))
		)
	)
	
	(method (onTarget)
		(return (<= (client distanceTo: who) distance))
	)
)
