;;; Sierra Script 1.0 - (do not remove this comment)
; This script contains some useful procedures for determining if something is in sight.
(script# 982)
(include sci.sh)
(use Main)

(public
	IsOnScreen 0
	CantBeSeen 1
	AngleDiff 2
)

;	
;	 Determines if an object is onscreen.
;	
;	 :param heapPtr theObj: An object with x and y properties.
;	 :returns: TRUE if the object is onscreen, FALSE otherwise.
(procedure (IsOnScreen theObj)
	(return
		(not
			(if
				(and
					(<= 0 (theObj x?))
					(<= (theObj x?) 319)
					(<= 0 (- (theObj y?) (theObj z?)))
				)
				(<= (- (theObj y?) (theObj z?)) 189)
			else
				0
			)
		)
	)
)

;
; .. function:: CantBeSeen(theSight [optSeer fieldAngle fieldDistance])
;
; 	Determines if an object can't be seen by another.
; 	
; 	:param heapPtr theSight: The object in question.
; 	:param heapPtr optSeer: The object we are asking, or the ego if not specified.
; 	:param number fieldAngle: The angle the looker object is looking, or lookerObj:sightAngle if not supplied.
; 	:param number fieldDistance: An optional distance limit.
; 	:returns: TRUE if the seer cannot see the sight, FALSE otherwise. FALSE if the sight is the seer.
(procedure (CantBeSeen theSight optSeer fieldAngle fieldDistance &tmp theSeer angleTemp distanceTemp sightX sightY seerX seerY)
	(= theSeer optSeer)
	(= angleTemp fieldAngle)
	(= distanceTemp fieldDistance)
	(if (< argc 4)
		(= distanceTemp 32767)
		(if (< argc 3)
			(if (< argc 2) (= theSeer gEgo))
			(= angleTemp
				(-
					360
					(if (== theSeer gEgo)
						(* 2 (theSeer sightAngle?))
					else
						0
					)
				)
			)
		)
	)
	(= sightX (theSight x?))
	(= sightY (theSight y?))
	(= seerX (theSeer x?))
	(= seerY (theSeer y?))
	(return
		(cond 
			((== theSight theSeer) FALSE)
			(
				(or
					(<
						(/ angleTemp 2)
						(Abs
							(AngleDiff
								(GetAngle seerX seerY sightX sightY)
								(theSeer heading?)
							)
						)
					)
					(<
						distanceTemp
						(GetDistance seerX seerY sightX sightY gPicAngle)
					)
				)
				(return TRUE)
			)
			(else (return FALSE))
		)
	)
)

;	
;	 Calculates the difference between two angles given in degrees.
;	
;	 :returns: The difference between the two angles, guarateed to be in the interval [-180,180).
(procedure (AngleDiff angle1 angle2)
	(if (>= argc 2) (= angle1 (- angle1 angle2)))
	(return
		(cond 
			((<= angle1 -180) (+ angle1 360))
			((> angle1 180) (- angle1 360))
			(else angle1)
		)
	)
)
