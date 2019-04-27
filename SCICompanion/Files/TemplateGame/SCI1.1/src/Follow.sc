;;; Sierra Script 1.0 - (do not remove this comment)
(script# 971)
(include sci.sh)
(use Cycle)


;	
;	 A motion class that is used to make an Actor move so it stays within a certain distance of another (possibly moving) object.
;	
;	 Example usage::	
;	
;	 	; Make the dog follow the ego at a distance of 20
;	 	(aDog setMotion: Follow gEgo 20)
;	
;	 Note that this class doesn't intelligently move the Actor around polygon obstacles. For that, use :class:`PFollow`.
(class Follow of Motion
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
		distance 20
	)
	
	;
	; .. function:: init(theClient theWho [theDistance])
	;
	; 	Initializes the Follow instance.
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param heapPtr theWho: The target to chase.
	; 	:param number theDistance: How close the client needs to get from the target.
	;
	(method (init theClient theWho theDistance)
		(if (>= argc 1)
			(= client theClient)
			(if (>= argc 2)
				(= who theWho)
				(if (>= argc 3) (= distance theDistance))
			)
		)
		(if (> (client distanceTo: who) distance)
			(super init: client (who x?) (who y?))
		)
	)
	
	(method (doit &tmp temp0)
		(if (> (client distanceTo: who) distance)
			(if (== b-moveCnt 0)
				(super init: client (who x?) (who y?))
			)
			(super doit:)
		else
			(= xLast (client x?))
			(= yLast (client y?))
			(= temp0 (GetAngle xLast yLast (who x?) (who y?)))
			(if (!= temp0 (client heading?))
				(client heading: temp0)
				(if (client looper?)
					((client looper?) doit: client temp0)
				else
					(DirLoop client temp0)
				)
			)
		)
	)
	
	(method (moveDone)
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
