;;; Sierra Script 1.0 - (do not remove this comment)
(script# FLIPPOLY_SCRIPT)
(include sci.sh)
(include game.sh)
(use Main)
(use System)

(public
	FlipPolygons 0
	FlipFeature 1
)

;
; .. function:: FlipPolygons()
;
; 	Flips the current room's polygons.
;
; .. function:: FlipPolygons(polygonCollection)
;
; .. function:: FlipPolygons(polygon)
;
; 	Flips a single polygon, or a collection of polygons.
(procedure (FlipPolygons param1 &tmp temp0)
	(cond 
		((not argc) (= temp0 (gRoom obstacles?)))
		((param1 isKindOf: Collection) (= temp0 param1))
		(else (param1 perform: flipPoly) (return))
	)
	(temp0 eachElementDo: #perform flipPoly)
	(DisposeScript FLIPPOLY_SCRIPT)
)

;
; .. function:: FlipFeature()
;
; 	Flips the current room's features.
;
; .. function:: FlipFeature(featureCollection)
;
; .. function:: FlipFeature(feature)
;
; 	Flips a single feature, or a collection of features.
(procedure (FlipFeature param1 &tmp temp0)
	(if (not argc)
		(gFeatures eachElementDo: #perform flipFeature)
	else
		(= temp0 0)
		(while (< temp0 argc)
			(if ([param1 temp0] isKindOf: Collection)
				([param1 temp0] eachElementDo: #perform flipFeature)
			else
				([param1 temp0] perform: flipFeature)
			)
			(++ temp0)
		)
	)
	(DisposeScript FLIPPOLY_SCRIPT)
)

(instance flipPoly of Code
	(properties)
	
	(method (doit param1 &tmp temp0 temp1 temp2)
		(= temp2 (param1 size?))
		(= temp1 (Memory memALLOC_CRIT (* 4 temp2)))
		(= temp0 0)
		(while (< temp0 temp2)
			(Memory
				memPOKE
				(+ temp1 (* 4 temp0))
				(-
					320
					(Memory
						memPEEK
						(-
							(+ (param1 points?) (* 4 temp2))
							(+ 4 (* 4 temp0))
						)
					)
				)
			)
			(Memory
				memPOKE
				(+ temp1 (* 4 temp0) 2)
				(Memory
					memPEEK
					(-
						(+ (param1 points?) (* 4 temp2))
						(+ 2 (* 4 temp0))
					)
				)
			)
			(++ temp0)
		)
		(if (param1 dynamic?)
			(Memory memFREE (param1 points?))
		)
		(param1 points: temp1 dynamic: 1)
	)
)

(instance flipFeature of Code
	(properties)
	
	(method (doit param1 &tmp temp0)
		(if (IsObject (param1 onMeCheck?))
			(FlipPolygons (param1 onMeCheck?))
		else
			(= temp0 (param1 nsLeft?))
			(param1
				nsLeft: (- 320 (param1 nsRight?))
				nsRight: (- 320 temp0)
			)
		)
	)
)
