;;; Sierra Script 1.0 - (do not remove this comment)
(script# 941)
(include sci.sh)
(use Main)
(use Cycle)


;	
;	 A cycler that oscillates back and forth between cels in a loop. See the init method for information on parameters.
;	
;	 Sample usage::
;	
;	 	; Make the star cycle between random cels 5 times.
;	 	(star setCycle: RandCycle 5)
(class RandCycle of Cycle
	(properties
		client 0
		caller 0
		cycleDir 1
		cycleCnt 0
		completed 0
		count -1
		reset 0
	)
	
	;
	; .. function:: init(theClient [theCount theCaller shouldReset])
	;
	; 	:param heapPtr theClient: The object to which the cycler is attached.
	; 	:param number theCount: Optional number of times to cycle. If not specified, or -1, cycling continues indefinitely.
	; 	:param heapPtr theCaller: Optional caller object that will be cue'd when the cycling is complete.
	; 	:param boolean shouldReset: Should the object go back to cel 0 after cycling is complete.
	;
	(method (init theClient theCount theCaller shouldReset)
		(super init: theClient)
		(if (>= argc 4) (= reset shouldReset))
		(if reset (client cel: 0))
		(= cycleCnt (GetTime))
		(if (>= argc 2)
			(if (!= theCount -1)
				(= count (+ (GetTime) theCount))
			else
				(= count -1)
			)
			(if (>= argc 3) (= caller theCaller))
		else
			(= count -1)
		)
	)
	
	(method (doit &tmp temp0)
		(= temp0 (GetTime))
		(if (or (> count temp0) (== count -1))
			(if (> (- temp0 cycleCnt) (client cycleSpeed?))
				(client cel: (self nextCel:))
				(= cycleCnt (GetTime))
			)
		else
			(if reset (client cel: 0))
			(self cycleDone:)
		)
	)
	
	(method (nextCel &tmp temp0)
		(return
			(if (!= (NumCels client) 1)
				(while
					(==
						(= temp0 (Random 0 (client lastCel:)))
						(client cel?)
					)
				)
				temp0
			else
				0
			)
		)
	)
	
	(method (cycleDone)
		(= completed 1)
		(if caller (= gDoMotionCue TRUE) else (self motionCue:))
	)
)
