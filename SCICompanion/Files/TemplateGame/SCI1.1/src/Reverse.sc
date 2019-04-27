;;; Sierra Script 1.0 - (do not remove this comment)
(script# REVERSECYCLE_SCRIPT)
(include sci.sh)
(include game.sh)
(use Cycle)


;	
;	 Reverse is a cycler that cycles through cels backward.
;	
;	 Example usage::
;	
;	 	(bird setCycle: Reverse)
;	
;	 See also: :class:`Forward`.
(class Reverse of Cycle
	(properties
		name {Rev}
		client 0
		caller 0
		cycleDir cdBACKWARD
		cycleCnt 0
		completed 0
	)
	
	(method (doit &tmp revNextCel)
		(= revNextCel (self nextCel:))
		(if (< revNextCel 0)
			(self cycleDone:)
		else
			(client cel: revNextCel)
		)
	)
	
	(method (cycleDone)
		(client cel: (client lastCel:))
	)
)
