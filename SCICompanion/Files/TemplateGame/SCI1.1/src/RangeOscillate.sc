;;; Sierra Script 1.0 - (do not remove this comment)
(script# 938)
(include sci.sh)
(use Cycle)


;	
;	 This cycler is similar to :class:`Oscillate`, but lets you specify specific first and last cels between which to oscillate (range oscillate).
;	
;	 Sample usage::
;	
;	 	; Make the star cycle 5 times between cel 2 and 7.
;	 	(star setCycle: RangeOscillate 5 2 7)
(class RangeOscillate of Cycle
	(properties
		name {ROsc}
		client 0
		caller 0
		cycleDir 1
		cycleCnt 0
		completed 0
		cycles -1
		firstC 0
		lastC 0
	)
	
	;	
	;	 :param heapPtr theClient: The :class:`Prop` to which this is attached.
	;	 :param number theCycles: The number of times to cycle, or -1 to cycle indefinitely.
	;	 :param number theFirstC: The cel at which to start.
	;	 :param number theLastC: The cel at which to end.
	;	 :param heapPtr theCaller: Optional object to be cue()'d when the cycle is complete.
	;	
	(method (init theClient theCycles theFirstC theLastC theCaller)
		(if (>= argc 2) (= cycles theCycles))
		(if (>= argc 5) (= caller theCaller))
		(super init: theClient)
		(if (>= argc 3)
			(= firstC theFirstC)
			(if (>= argc 4)
				(if theLastC
					(= lastC theLastC)
				else
					(= lastC (client lastCel:))
				)
			else
				(= lastC (client lastCel:))
			)
		)
		(client cel: firstC)
	)
	
	(method (doit &tmp rOscNextCel)
		(= rOscNextCel (self nextCel:))
		(if
		(or (> rOscNextCel lastC) (< rOscNextCel firstC))
			(= cycleDir (- cycleDir))
			(self cycleDone:)
		else
			(client cel: rOscNextCel)
		)
	)
	
	(method (cycleDone)
		(if cycles
			(client cel: (self nextCel:))
			(if (> cycles 0) (-- cycles))
		else
			(= completed 1)
			(self motionCue:)
		)
	)
)
