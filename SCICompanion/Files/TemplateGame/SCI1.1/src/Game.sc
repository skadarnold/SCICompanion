;;; Sierra Script 1.0 - (do not remove this comment)
(script# 994)
(include sci.sh)
(include game.sh)
(use Main)
(use Print)
(use Polygon)
(use Sound)
(use SaveRestoreDialog)
(use Cycle)
(use InventoryItem)
(use ScrollableInventory)
(use User)
(use System)


(procedure (PromptForDiskChange param1 &tmp temp0 [temp1 40] [temp41 40] temp81 [temp82 40] [temp122 10] [temp132 5])
	(= temp81 (Memory memALLOC_CRIT 150))
	(= temp0 1)
	(DeviceInfo 0 gSaveDir @temp1)
	(DeviceInfo 1 @temp41)
	(if
		(and
			(DeviceInfo 3 @temp41)
			(or
				(DeviceInfo 2 @temp1 @temp41)
				(not (DeviceInfo 6 (gGame name?)))
			)
		)
		(Message msgGET 994 6 0 0 1 @temp82)
		(Message msgGET 994 7 0 0 1 @temp122)
		(Message msgGET 994 8 0 0 1 @temp132)
		(Format
			temp81
			@temp82
			(if param1 @temp122 else @temp132)
			@temp1
		)
		(Load rsFONT gFont)
		(DeviceInfo 4)
		(Message msgGET 994 2 0 0 1 @temp82)
		(Message msgGET 994 4 0 0 1 @temp122)
		(Message msgGET 994 5 0 0 1 @temp132)
		(= temp0
			(if param1
				(Print
					font: 0
					addText: temp81
					addButton: 1 @temp82 0 40
					addButton: 0 @temp122 30 40
					addButton: 2 @temp132
					init:
				)
			else
				(Print
					font: 0
					addText: temp81
					addButton: 1 @temp82 0 40
					init:
				)
			)
		)
		(if (== temp0 2) (= temp0 (GetDirectory gSaveDir)))
	)
	(Memory memFREE temp81)
	(return temp0)
)

(instance cast of EventHandler
	(properties)
)

(instance features of EventHandler
	(properties)
)

(instance theDoits of EventHandler
	(properties)
)

;	
;	 This is a static class used to contain all sounds in the game.
;	 It is accessed through gSounds and allows pausing or unpausing all game sounds at once.
;	
;	 Example usage::
;	
;	 	(gSounds pause: TRUE)
(class Sounds of EventHandler
	(properties
		elements 0
		size 0
	)
	
	(method (pause fPause)
		(self
			eachElementDo: #perform mayPause (if argc fPause else 1)
		)
	)
)

(instance mayPause of Code
	(properties)
	
	(method (doit param1 param2)
		(if (not (& (param1 flags?) $0001))
			(param1 pause: param2)
		)
	)
)

(instance regions of EventHandler
	(properties)
)

(instance addToPics of EventHandler
	(properties)
	
	(method (doit)
		(self eachElementDo: #perform aTOC)
		(AddToPic elements)
	)
)

(instance timers of Set
	(properties)
)

(instance mouseDownHandler of EventHandler
	(properties)
)

(instance keyDownHandler of EventHandler
	(properties)
)

(instance directionHandler of EventHandler
	(properties)
)

(instance walkHandler of EventHandler
	(properties)
)

;	
;	 This class is used internally by :class:`Messager`.
(class Cue of Object
	(properties
		cuee 0
		cuer 0
		register 0
	)
	
	(method (doit)
		(gCuees delete: self)
		(if (gCuees isEmpty:) (gCuees dispose:) (= gCuees 0))
		(cuee cue: register cuer)
		(self dispose:)
	)
)

(instance aTOC of Code
	(properties)
	
	(method (doit param1 &tmp temp0 temp1)
		(if (not (& (param1 signal?) $4000))
			(= temp0
				(+ (gEgo xStep?) (/ (CelWide (gEgo view?) 2 0) 2))
			)
			(= temp1 (* (gEgo yStep?) 2))
			(gRoom
				addObstacle:
					((Polygon new:)
						init:
							(- (param1 brLeft?) temp0)
							(- (CoordPri 1 (CoordPri (param1 y?))) temp1)
							(+ (param1 brRight?) temp0)
							(- (CoordPri 1 (CoordPri (param1 y?))) temp1)
							(+ (param1 brRight?) temp0)
							(+ (param1 y?) temp1)
							(- (param1 brLeft?) temp0)
							(+ (param1 y?) temp1)
						yourself:
					)
			)
		)
	)
)

;	
;	 This contains the base game functionality. Most of the time, these methods are just called by the framework.
;	 The main interesting ones you might need to call from code are:
;	
;	 handsOn()
;	 	Gives the user control of the game.
;	
;	 handsOff()
;	 	Removes control of the game from the user (e.g. for cutscenes)
;	
;	 setCursor()
;	 	Sets the current game cursor.
(class Game of Object
	(properties
		script 0
		printLang 1
		_detailLevel 3
		panelObj 0          ; Current object target of a UI control
		panelSelector 0     ; Selector for the object target.
		handsOffCode 0
		handsOnCode 0
	)
	
	(method (init)
		(= gCast cast)
		(gCast add:)
		(= gFeatures features)
		(gFeatures add:)
		(= gSounds Sounds)
		(gSounds add:)
		(= gRegions regions)
		(gRegions add:)
		(= gAddToPics addToPics)
		(gAddToPics add:)
		(= gTimers timers)
		(gTimers add:)
		(= gTheDoits theDoits)
		(gTheDoits add:)
		(= gOldMH mouseDownHandler)
		(gOldMH add:)
		(= gOldKH keyDownHandler)
		(gOldKH add:)
		(= gOldDH directionHandler)
		(gOldDH add:)
		(= gWalkHandler walkHandler)
		(gWalkHandler add:)
		(= gFastCast 0)
		(= gSaveDir (GetSaveDir))
		(InventoryBase init:)
		(if (not gUser) (= gUser User))
		(gUser init:)
	)
	
	(method (doit &tmp newEvent thePanelObj thePanelSelector)
		(if panelObj
			(= thePanelObj panelObj)
			(= thePanelSelector panelSelector)
			(= panelObj (= panelSelector 0))
			(Eval thePanelObj thePanelSelector)
		)
		(= gGameTime (+ gTickOffset (GetTime)))
		(if gFastCast
			(while gFastCast
				(gFastCast eachElementDo: #doit)
				(= newEvent (Event new:))
				(if (and (newEvent type?) gFastCast)
					(gFastCast firstTrue: #handleEvent newEvent)
				)
				(newEvent dispose:)
				(= gGameTime (+ gTickOffset (GetTime)))
				(gSounds eachElementDo: #check)
			)
		)
		(if gPrints
			(gPrints eachElementDo: #doit)
			(if (not gDialog)
				(= newEvent (Event new:))
				(if (and (newEvent type?) gPrints)
					(gPrints firstTrue: #handleEvent newEvent)
				)
				(newEvent dispose:)
				(= gGameTime (+ gTickOffset (GetTime)))
				(return)
			)
		)
		(gSounds eachElementDo: #check)
		(gTimers eachElementDo: #doit)
		(if (and gDialog (gDialog check:)) (gDialog dispose:))
		(Animate (gCast elements?) 1)
		(if gDoMotionCue
			(= gDoMotionCue FALSE)
			(gCast eachElementDo: #motionCue)
		)
		(if gCuees (gCuees eachElementDo: #doit))
		(if script (script doit:))
		(gRegions eachElementDo: #doit)
		(if gFastCast (return))
		(if (== gNewRoomNumber gRoomNumber) (gUser doit:))
		(gTheDoits doit:)
		(if (!= gNewRoomNumber gRoomNumber)
			(self newRoom: gNewRoomNumber)
		)
		(gTimers eachElementDo: #delete)
		(GameIsRestarting FALSE)
	)
	
	(method (play)
		(= gGame self)
		(= gSaveDir (GetSaveDir))
		(self setCursor: gWaitCursor 1 init:)
		(self setCursor: gNormalCursor 1)
		(while (not gQuitGame)
			(self doit:)
		)
	)
	
	(method (replay &tmp temp0)
		(if gPEvent (gPEvent dispose:))
		(if gDialog (gDialog dispose:))
		(gCast eachElementDo: #perform RU)
		(gGame setCursor: gWaitCursor 1)
		(= temp0
			(if
				(not
					(IsOneOf
						(gRoom style?)
						-1
						dpOPEN_SCROLL_RIGHT
						dpOPEN_SCROLL_LEFT
						dpOPEN_SCROLL_UP
						dpOPEN_SCROLL_DOWN
					)
				)
				(gRoom style?)
			else
				dpOPEN_NO_TRANSITION
			)
		)
		(DrawPic (gRoom curPic?) temp0 dpCLEAR)
		(if (!= gPicNumber -1)
			(DrawPic gPicNumber 100 dpNO_CLEAR)
		)
		(gAddToPics doit:)
		(cond 
			(
				(and
					(not (gUser canControl:))
					(not (gUser canInput:))
				)
				(gGame setCursor: gWaitCursor)
			)
			((and gIconBar (gIconBar curIcon?)) (gGame setCursor: ((gIconBar curIcon?) cursor?)))
			(else (gGame setCursor: gNormalCursor))
		)
		(DoSound sndRESTORE)
		(gSounds pause: FALSE)
		(= gTickOffset (- gGameTime (GetTime)))
		(while (not gQuitGame)
			(self doit:)
		)
	)
	
	;
	; Changes to a new room.
	;
	; .. IMPORTANT::
	;    To change to a new room, you should *not* use this method. Instead, use the newRoom method on the current room, e.g::
	;
	;    	(gRoom newRoom: 105)
	;
	(method (newRoom newRoomNumber &tmp [temp0 5] temp5)
		(DebugPrint {Switching to room %d} newRoomNumber)
		(gAddToPics
			eachElementDo: #dispose
			eachElementDo: #delete
			release:
		)
		(gFeatures eachElementDo: #perform fDC release:)
		(gCast eachElementDo: #dispose eachElementDo: #delete)
		(gTimers eachElementDo: #delete)
		(gRegions
			eachElementDo: #perform DisposeNonKeptRegion
			release:
		)
		(gTheDoits release:)
		(Animate 0)
		(= gPreviousRoomNumber gRoomNumber)
		(= gRoomNumber newRoomNumber)
		(= gNewRoomNumber newRoomNumber)
		(FlushResources newRoomNumber)
		(self startRoom: gRoomNumber)
		(while ((= temp5 (Event new: 3)) type?)
			(temp5 dispose:)
		)
		(temp5 dispose:)
	)
	
	(method (startRoom param1)
		(if gDebugOnNextRoom (SetDebug))
		(= gRoom (ScriptID param1))
		(gRegions addToFront: gRoom)
		(gRoom init:)
	)
	
	(method (restart)
		(if gDialog (gDialog dispose:))
		(RestartGame)
	)
	
	(method (restore &tmp [temp0 20] temp20 temp21 [temp22 100] [temp122 5] [temp127 100])
		(if (not (ValidPath gSaveDir))
			(Message msgGET 994 9 0 0 1 @temp22)
			(Format @temp127 @temp22 gSaveDir)
			(Print font: 0 addText: @temp127 init:)
			(GetDirectory gSaveDir)
		)
		(Load rsFONT gSmallFont)
		(ScriptID SAVERESTORE_SCRIPT)
		(= temp21 (self setCursor: gNormalCursor))
		(gSounds pause: TRUE)
		(if (PromptForDiskChange 1)
			(if gDialog (gDialog dispose:))
			(= temp20 (Restore doit: &rest))
			(if (!= temp20 -1)
				(self setCursor: gWaitCursor 1)
				(if (CheckSaveGame name temp20 gVersion)
					(RestoreGame name temp20 gVersion)
				else
					(Message msgGET 994 3 0 0 1 @temp22)
					(Message msgGET 994 2 0 0 1 @temp122)
					(Print
						font: 0
						addText: @temp22
						addButton: 1 @temp122 0 40
						init:
					)
					(self setCursor: temp21 (HaveMouse))
				)
			)
			(PromptForDiskChange 0)
		)
		(gSounds pause: FALSE)
	)
	
	(method (save &tmp [temp0 20] temp20 temp21 [temp22 100] [temp122 5] [temp127 100])
		(if (not (ValidPath gSaveDir))
			(Message msgGET 994 9 0 0 1 @temp22)
			(Format @temp127 @temp22 gSaveDir)
			(Print font: 0 addText: @temp127 init:)
			(GetDirectory gSaveDir)
		)
		(Load rsFONT gSmallFont)
		(ScriptID SAVERESTORE_SCRIPT)
		(= temp21 (self setCursor: gNormalCursor))
		(gSounds pause: TRUE)
		(if (PromptForDiskChange 1)
			(if gDialog (gDialog dispose:))
			(= temp20 (Save doit: @temp0))
			(if (!= temp20 -1)
				(= temp21 (self setCursor: gWaitCursor 1))
				(if (not (SaveGame name temp20 @temp0 gVersion))
					(Message msgGET 994 1 0 0 1 @temp22)
					(Message msgGET 994 2 0 0 1 @temp122)
					(Print
						font: 0
						addText: @temp22
						addButton: 1 @temp122 0 40
						init:
					)
				)
				(self setCursor: temp21 (HaveMouse))
			)
			(PromptForDiskChange 0)
		)
		(gSounds pause: FALSE)
	)
	
	(method (handleEvent pEvent)
		(cond 
			((pEvent claimed?) 1)
			((and script (script handleEvent: pEvent)) 1)
			((& (pEvent type?) evVERB) (self pragmaFail:))
		)
		(pEvent claimed?)
	)
	
	(method (showMem &tmp [temp0 100])
		(Format
			@temp0
			{Free Heap: %u Bytes\nLargest ptr: %u Bytes\nFreeHunk: %u KBytes\nLargest hunk: %u Bytes}
			(MemoryInfo 1)
			(MemoryInfo 0)
			(>> (MemoryInfo 3) $0006)
			(MemoryInfo 2)
		)
		(Print addText: @temp0 init:)
	)
	
	(method (setCursor cursorNumber isVisible cursorX cursorY hotspotX hotspotY &tmp theGCursorNumber)
		(= theGCursorNumber gCursorNumber)
		(if (IsObject cursorNumber)
			(= gCursorNumber cursorNumber)
			(cursorNumber init:)
		else
			(SetCursor cursorNumber 0 0)
		)
		(if (> argc 1)
			(SetCursor isVisible)
			(if (> argc 2)
				(SetCursor cursorX cursorY)
				(if (> argc 4)
					(SetCursor cursorNumber 0 0 hotspotX hotspotY)
				)
			)
		)
		(return theGCursorNumber)
	)
	
	(method (notify)
	)
	
	(method (setScript theScript)
		(if script (script dispose:))
		(if theScript (theScript init: self &rest))
	)
	
	(method (cue)
		(if script (script cue:))
	)
	
	(method (quitGame param1)
		(if (or (not argc) param1) (= gQuitGame TRUE))
	)
	
	(method (masterVolume param1)
		(if argc
			(DoSound sndMASTER_VOLUME param1)
		else
			(DoSound sndMASTER_VOLUME)
		)
	)
	
	(method (detailLevel the_detailLevel)
		(if argc
			(= _detailLevel the_detailLevel)
			(gCast eachElementDo: #checkDetail)
		)
		(return _detailLevel)
	)
	
	(method (pragmaFail)
	)
	
	;	
	;	 Removes control of the game from the user (for cutscenes).
	;	
	;	 Example usage::
	;	
	;	 	(gGame handsOff:)
	;	
	(method (handsOff)
		(if handsOffCode
			(handsOffCode doit: &rest)
		else
			(User canControl: 0 canInput: 0)
			(if (IsObject gEgo) (gEgo setMotion: 0))
		)
	)
	
	;	
	;	 Gives the user control of the game.
	;	
	;	 Example usage::
	;	
	;	 	(gGame handsOn:)
	;	
	(method (handsOn)
		(if handsOnCode
			(handsOnCode doit: &rest)
		else
			(User canControl: 1 canInput: 1)
		)
	)
)

;	
;	 Region serves as a base class for :class:`Rm`. It can also be used on its own, to contain
;	 common logic that applies to several rooms.
;	
;	 To make a region part of a room, put the following code in the room's init() method::
;	
;	 	(self setRegions: MY_REGION)
;	 	; Where MY_REGION would be a script number for a script that contains a public Rgn instance.
(class Region of Object
	(properties
		name {Rgn}
		script 0
		number 0
		modNum -1
		noun 0
		_case 0
		timer 0
		keep 0
		initialized 0
	)
	
	;	
	;	 Override this method to provide any custom initialization needed for your Rgn. Make sure you call (super:init()), or
	;	 your region won't be added to the current room's regions.
	;	
	(method (init)
		(if (not initialized)
			(= initialized 1)
			(if (not (gRegions contains: self))
				(gRegions addToEnd: self)
			)
			(super init:)
		)
	)
	
	;	
	;	 This is commonly overridden in Rgn instances to provide custom logic that gets executed every game cycle.
	;	
	(method (doit)
		(if script (script doit:))
	)
	
	(method (dispose)
		(gRegions delete: self)
		(if (IsObject script) (script dispose:))
		(if (IsObject timer) (timer dispose: delete:))
		(gSounds eachElementDo: #clean self)
		(DisposeScript number)
	)
	
	;	
	;	 This is commonly overridden in Rgn instances to handle keyboard or mouse events.
	;	
	(method (handleEvent pEvent)
		(cond 
			((pEvent claimed?) 1)
			((& (pEvent type?) evJOYSTICK) 0)
			(
				(not
					(if
					(and script (or (script handleEvent: pEvent) 1))
						(pEvent claimed?)
					)
				)
				(pEvent claimed: (self doVerb: (pEvent message?)))
			)
		)
		(pEvent claimed?)
	)
	
	;	
	;	 This is commonly overridden in Rgn instances to code custom logic for verbs.
	;	
	(method (doVerb theVerb &tmp temp0)
		(if (== modNum -1) (= modNum gRoomNumber))
		(return
			(if (Message msgGET modNum noun theVerb _case 1)
				(gMessager say: noun theVerb _case 0 0 modNum)
				1
			else
				0
			)
		)
	)
	
	;
	; .. function:: setScript(theScript [sendParams ...])
	;
	; 	This attaches a :class:`Script` to the Rgn.
	;
	; 	:param heapPtr theScript: A Script instance.
	; 	:param sendParams: Any additional parameters needed to initialize the script.
	;
	(method (setScript theScript)
		(if (IsObject script) (script dispose:))
		(if theScript (theScript init: self &rest))
	)
	
	(method (cue)
		(if script (script cue:))
	)
	
	(method (newRoom)
	)
	
	(method (notify)
	)
)

;	
;	 Room is the class that your room instances should inherit from.
(class Room of Region
	(properties
		name {Rm}
		script 0        ; Room script, generally set with setScript in the init() method.
		number 0
		modNum -1
		noun 0          ; Noun associated with the room background.
		_case 0         ; Case associated with the room background.
		timer 0
		keep 0
		initialized 0
		picture 0       ; The number of the pic resource for the room background.
		style $ffff     ; The dp\* animation flags that control room opening animation.
		horizon 0       ; y coordinate of room's horizon.
		controls 0
		north 0         ; The room to the north (if appropriate)
		east 0          ; The room to the east (if appropriate)
		south 0         ; The room to the south (if appropriate)
		west 0          ; The room to the west (if appropriate)
		curPic 0
		picAngle 0      ; How far from vertical is the view? 0-89
		vanishingX 160
		vanishingY 0
		obstacles 0
		inset 0
	)
	
	;	
	;	 Override this method to provide the needed initialization for your room. Make sure to call (super:init()).
	;	
	(method (init)
		(= number gRoomNumber)
		(= gPicAngle picAngle)
		(if picture (self drawPic: picture))
		(self
			reflectPosn: (gUser alterEgo?) ((gUser alterEgo?) edgeHit?)
		)
		((gUser alterEgo?) edgeHit: 0)
	)
	
	;	
	;	 Override this method to provide custom logic that is executed every game cycle for your room. Make sure you call (super:doit()).
	;	
	(method (doit &tmp temp0)
		(if script (script doit:))
		(= temp0
			(self edgeToRoom: ((gUser alterEgo?) edgeHit?))
		)
		(if temp0
			(self
				newRoom: (= temp0
					(self edgeToRoom: ((gUser alterEgo?) edgeHit?))
				)
			)
		)
	)
	
	(method (dispose)
		(if obstacles (obstacles dispose:))
		(super dispose:)
	)
	
	;	
	;	 Override this method to provide custom event handling logic for your room. Make sure you call (super:handleEvent(pEvent)).
	;	
	(method (handleEvent pEvent)
		(if (and inset (inset handleEvent: pEvent))
		else
			(super handleEvent: pEvent)
		)
		(pEvent claimed?)
	)
	
	;	
	;	 This tells the room that we want to switch to a new room. This is the main way to switch rooms.
	;	
	;	 Example usage::
	;	
	;	 	(gRoom newRoom: 145)
	;	
	(method (newRoom newRoomNumber)
		(gRegions
			delete: self
			eachElementDo: #newRoom newRoomNumber
			addToFront: self
		)
		(= gNewRoomNumber newRoomNumber)
		(super newRoom: newRoomNumber)
	)
	
	;	
	;	 Lets you indicate which regions this room is part of.
	;	
	;	 :param number ScriptNumbers: One or more script numbers for scripts that contain a public :class:`Rgn` instance.
	;	
	;	 Example usage::
	;	
	;	 	(method (init)
	;	 		(self setRegions: SNOW_REGION MOUNTAIN_REGION)
	;	 		; etc, do more room initialization...
	;	 		(super init:)
	;	 	)
	;	
	(method (setRegions scriptNumbers &tmp temp0 theScriptNumbers temp2)
		(= temp0 0)
		(while (< temp0 argc)
			(= theScriptNumbers [scriptNumbers temp0])
			(= temp2 (ScriptID theScriptNumbers))
			(temp2 number: theScriptNumbers)
			(gRegions add: temp2)
			(if (not (temp2 initialized?)) (temp2 init:))
			(++ temp0)
		)
	)
	
	;
	; .. function:: drawPic(picNumber [picAnimation])
	;
	; 	Draws a pic background.
	;
	; 	:param number picNumber: The number of the pic resource.
	; 	:param number picAnimation: An optional dp\* enum value (e.g. dpOPEN_INSTANTLY).
	;
	; 	See the :func:`DrawPic` kernel for more information on animation flags.
	;
	(method (drawPic picNumber picAnimation)
		(if gAddToPics
			(gAddToPics eachElementDo: #dispose release:)
		)
		(= curPic picNumber)
		(= gPicNumber -1)
		(DrawPic
			picNumber
			(cond 
				((== argc 2) picAnimation)
				((!= style -1) style)
				(else 100)
			)
			dpCLEAR
		)
	)
	
	;
	; .. function:: overlay(picNumber [picAnimation])
	;
	; 	Overlays an additional pic on top of the current background.
	;
	; 	:param number picNumber: The number of the pic resource.
	; 	:param number picAnimation: An optional dp\* enum value (e.g. dpOPEN_INSTANTLY).
	;
	(method (overlay picNumber picAnimation)
		(= gPicNumber picNumber)
		(DrawPic
			picNumber
			(cond 
				((== argc 2) picAnimation)
				((!= style -1) style)
				(else 100)
			)
			dpNO_CLEAR
		)
	)
	
	;	
	;	 This can be used to add polygon obstacles to the current room.
	;	
	;	 Example usage::
	;	
	;	 	(gRoom addObstacle: (&getpoly "Flower"))
	;	
	(method (addObstacle polygon)
		(if (not (IsObject obstacles))
			(= obstacles (List new:))
		)
		(obstacles add: polygon &rest)
	)
	
	;	
	;	 Assigns the actor a new position based on the edge that it left from
	;	 in the previous room.
	;	
	(method (reflectPosn theActor theEdgeHit)
		(switch theEdgeHit
			(EDGE_TOP (theActor y: 188))
			(EDGE_LEFT
				(theActor x: (- 319 (theActor xStep?)))
			)
			(EDGE_BOTTOM
				(theActor y: (+ horizon (theActor yStep?)))
			)
			(EDGE_RIGHT (theActor x: 1))
		)
	)
	
	;	
	;	 :param number theEdgeHit: EDGE_TOP, EDGE_LEFT, EDGE_RIGHT or EDGE_BOTTOM.
	;	 :returns: The room number associated with that edge.
	;	
	(method (edgeToRoom theEdgeHit)
		(switch theEdgeHit
			(EDGE_TOP north)
			(EDGE_RIGHT east)
			(EDGE_BOTTOM south)
			(EDGE_LEFT west)
		)
	)
	
	;	
	;	 :param number roomNumber: A room number.
	;	 :returns: The edge associated with that room number (or the room number if no edge associated).
	;	
	(method (roomToEdge roomNumber)
		(switch roomNumber
			(north EDGE_TOP)
			(south EDGE_BOTTOM)
			(east EDGE_RIGHT)
			(west EDGE_LEFT)
		)
	)
	
	;
	; .. function:: setInset([theInset theCaller theRegister])
	;
	; 	Sets an :class:`Inset` on this room. To clear the inset, pass no parameters.
	;
	; 	:param heapPtr theInset: The Inset instance.
	; 	:param heapPtr theCaller: An object that will get cue()'d when the Inset is disposed.
	;
	; 	Example usage::
	;
	; 		(gRoom setInset: cageCloseUp self)
	;
	(method (setInset theInset theCaller theRegister)
		(if inset (inset dispose:))
		(if (and argc theInset)
			(theInset
				init:
					(if (>= argc 2) theCaller else 0)
					self
					(if (>= argc 3) theRegister else 0)
			)
		)
	)
)

(instance RU of Code
	(properties)
	
	(method (doit param1 &tmp temp0)
		(if (param1 underBits?)
			(= temp0 (param1 signal?))
			(= temp0 (| temp0 $0001))
			(= temp0 (& temp0 $fffb))
			(param1 underBits: 0 signal: temp0)
		)
	)
)

(instance DisposeNonKeptRegion of Code
	(properties)
	
	(method (doit param1)
		(if (not (param1 keep?)) (param1 dispose:))
	)
)

(instance fDC of Code
	(properties)
	
	(method (doit param1)
		(if (param1 respondsTo: #delete)
			(param1
				signal: (& (param1 signal?) $ffdf)
				dispose:
				delete:
			)
		else
			(param1 dispose:)
		)
	)
)
