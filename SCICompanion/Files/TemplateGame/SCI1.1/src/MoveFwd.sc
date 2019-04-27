;;; Sierra Script 1.0 - (do not remove this comment)
(script# MOVEFORWARD_SCRIPT)
(include sci.sh)
(include game.sh)
(use PolyPath)


;	
;	 This is a :class:`Motion` class that moves an Actor forward some amount.
;	
;	 Example usage::
;	
;	 	; Move the ego forward 50 pixels, and cue the calling script when done.
;	 	(gEgo setMotion: MoveFwd 50 self)
(class MoveFwd of PolyPath
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
	)
	
	;
	; .. function:: init(theClient distance [theCaller])
	;
	; 	Initializes the MoveFwd instance.
	;
	; 	:param heapPtr theClient: The :class:`Actor` to which this is attached.
	; 	:param number distance: The distance to move forward.
	; 	:param heapPtr theCaller: Optional object on which cue() will be called when the target is reached.
	;
	(method (init theClient distance theCaller)
		(if argc
			(super
				init:
					theClient
					(+
						(theClient x?)
						(SinMult (theClient heading?) distance)
					)
					(-
						(theClient y?)
						(CosMult (theClient heading?) distance)
					)
					(if (>= argc 3) theCaller else 0)
			)
		else
			(super init:)
		)
	)
)
