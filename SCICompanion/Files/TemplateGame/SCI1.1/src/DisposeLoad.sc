;;; Sierra Script 1.0 - (do not remove this comment)
(script# DISPOSELOAD_SCRIPT)
(include sci.sh)
(include game.sh)

(public
	DisposeLoad 0
)

;	
;	 This procedure can be used to preload a series of resources in a room so there is no delay when 
;	 they are actually used. With modern hardware, there is probably no reason to use this in your game.
;	
;	 It can also be used to dispose of loaded scripts if resourceType is set to zero. This functionality is
;	 used by the disposeCode.sc script.
(procedure (DisposeLoad resourceType resourceNumber &tmp temp0 temp1)
	(= argc (- argc 2))
	(= temp0 0)
	(while (<= temp0 argc)
		(= temp1 [resourceNumber temp0])
		(if resourceType
			(Load resourceType temp1)
		else
			(DisposeScript temp1)
		)
		(++ temp0)
	)
	(DisposeScript DISPOSELOAD_SCRIPT)
)
