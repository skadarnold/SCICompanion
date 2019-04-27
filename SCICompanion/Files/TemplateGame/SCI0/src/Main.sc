;;; Sierra Script 1.0 - (do not remove this comment)
;
; SCI Template Game
; By Brian Provinciano
; ******************************************************************************
; main.sc
; Contains the game's main instance and inventory items.
(script# MAIN_SCRIPT)
(include sci.sh)
(include game.sh)
(use controls)
(use sound)
(use syswindow)
(use cycle)
(use game)
(use inv)
(use user)
(use menubar)
(use feature)
(use obj)
(use disposeload)

(public
	Template 0
	SetUpEgo 1
	SetUpActor 2
	ProgramControl 3
	PlayerControl 4
	DisposePrintDlg 5
	PrintOK 6
	PrintItIs 7
	PrintYouAre 8
	PrintGoodIdea 9
	PrintNotCloseEnough 10
	PrintAlreadyTookIt 11
	PrintDontHaveIt 12
	PrintCantDoThat 13
	AddViewToPic 14
	SetInvIOwner 15
	DebugPrint 16
)

(local


;
; * These are the global variables. You can access them from any script as long
; * as it "use"es this script
	gEgo
; points to the ego's class
	gGame
; points to the game instance
	gRoom
; points to the current room instance
	gSpeed
; the game speed (delay each interpreter cycle)
	gQuitGame =  FALSE
; if set to TRUE, the game will exit
	gCast
; points to the cast class (list of actors)
	gRegions
; points to the regions class (list of regions)
	gLocales
; points to the locales class (list of locales)
	gTimers
; points to the timers class (list of timers)
	gSounds
; points to the sounds class (list of sounds)
	gInv
; points to the inventory class
	gAddToPics
; points to the add to pics class
	gFeatures
; points to the add to features class
	gSFeatures
; points to the add to sfeatures class
	gRoomNumberExit
; room number exit
	gPreviousRoomNumber
; the number of the previous room
	gRoomNumber
; the number of the current room
	gDebugOnExit =  FALSE
; enter debug mode on room exit
	gScore =  0
; the game score
	gMaxScore =  0
; the maximum game score
	gOldScore
; previous score
	gCurrentCursor
; the number of the current cursor
	gNormalCursor =  999
; the number of the normal cursor (ie. arrow)
	gLoadingCursor =  997
; the number of the loading cursor (ie. hand)
	gDefaultFont =  1
; the number of the default font
	gSaveRestoreFont =  0
; the number of the font for the save/restore dialogs
	gDeadFont =  0
; the number of the font for the dialog when ego dies
	gUserEvent
; points to the user's event object
	gPrintDlg
; points to the current Print() window
	gVolume
; the sound volume
	gVersion
; the version string
	gSaveDirPtr
; points to the save directory string
	gCheckAniWait
; the checkAni delay
	gSetRegions
; a flag -- see User:doit()
	gPicAngle
; the room's pic angle
	gOverlay =  -1
; whether to overlay the picture when drawing
	gDefaultPicAni
; the default pic animation
	gDefaultPalette
; the default palette to use for the pictures (0-3)
	gCastMotionCue
; if set, the cast's motionCue() is called
	gTheWindow
; points to the window class
	gWndColor
; the colour of the game's windows foreground (ie. text)
	gWndBack
; the colour of the game's windows background
	gOldPort
; the previous port
	gEgoView
; ego's current view number
; hh:mm:ss | gTimeHours:gTimeMinutes:gTimeSeconds
; the time elapsed since the game started
	gTimeSeconds
; the seconds
	gTimeMinutes
; the minutes
	gTimeHours
; the hours
	gCurrentTime
; the current time in seconds
	gTheMusic
; points to the music class
	gTheSoundFX
; points to the sound fx class
	gProgramControl
; states whether the program has control or the user
	gDebugOut =  0
; The support for SCI Companion debugging
	gDebugStartRoom =  -1
)

(instance Template of Game
	(properties)
	
	(method (init &tmp theStartRoom debugHandle)
		; Set up the base window
		(= gTheWindow theWindow)
		(= gWndColor clBLACK)
		(= gWndBack clWHITE)
		(gTheWindow color: gWndColor back: gWndBack)
		; Initialize
		(super init:)
;
;         * Set your game version here *
		(= gVersion {1.0})
		; General initialization stuff
		(= gVolume 15)
		(DoSound sndVOLUME gVolume)
		(SL code: statusCode)
		(TheMenuBar init:)
		(scoreSound owner: self init:)
		(= gTheMusic theMusic)
		(gTheMusic owner: self init:)
		(= gTheSoundFX theSoundFX)
		(gTheSoundFX owner: self init:)
		(= gEgo ego)
		(User alterEgo: gEgo blocks: 0 y: 150)
		(Load rsFONT gDeadFont)
		(Load rsFONT gDefaultFont)
		(Load rsFONT gSaveRestoreFont)
		(Load rsCURSOR gNormalCursor)
		(Load rsCURSOR gLoadingCursor)
		(if (HaveMouse)
			(gGame setCursor: gNormalCursor SET_CURSOR_VISIBLE)
		else
			(gGame
				setCursor: gNormalCursor SET_CURSOR_VISIBLE 304 174
			)
		)
;
;         * Initialize the inventory with it's items here *
		(Inv add: Nothing Test_Object)
		; Start the room
		(= theStartRoom TITLESCREEN_SCRIPT)
		(= gDebugStartRoom -1)
		(if (not (GameIsRestarting))
			(if
			(= debugHandle (FOpen {sdebug.txt} fOPENCREATE))
				(FClose debugHandle) ; we're just testing for existence - the SCI0 way
				(= gDebugOut (ScriptID DEBUGOUT_SCRIPT 0))
				(DebugPrint {Debugger enabled})
				(= gDebugStartRoom (gDebugOut init: {sdebug.txt}))
				(if (!= gDebugStartRoom -1)
					(= theStartRoom INITROOMS_SCRIPT)
					(DebugPrint {Starting in room %d} gDebugStartRoom)
				)
			)
		else
			(= theStartRoom INITROOMS_SCRIPT)
		)
		(self newRoom: theStartRoom)
	)
	
	(method (doit)
		(super doit:)
		(if gProgramControl
			(User canControl: FALSE canInput: FALSE)
		)
		(if
			(!=
				gCurrentTime
				(= gCurrentTime (GetTime gtTIME_OF_DAY))
			)
			(if (>= (++ gTimeSeconds) 60)
				(= gTimeSeconds 0)
				(++ gTimeMinutes)
				(if (>= gTimeMinutes 60)
					(= gTimeMinutes 0)
					(++ gTimeHours)
				)
			)
		)
		(if (> gOldScore gScore)
			(= gOldScore gScore)
			(SL doit:)
		)
		(if (< gOldScore gScore)
			(= gOldScore gScore)
			(SL doit:)
		)
	)
	
	(method (replay)
		(TheMenuBar draw:)
		(SL enable:)
		(if (DoSound sndSET_SOUND)
			(SetMenu MENU_TOGGLESOUND #text {Turn Off})
		else
			(SetMenu MENU_TOGGLESOUND #text {Turn On})
		)
		(super replay:)
	)
	
	(method (newRoom roomNum picAni)
		(DisposePrintDlg)
		(Load rsFONT gDeadFont)
		(Load rsFONT gDefaultFont)
		(Load rsFONT gSaveRestoreFont)
		(Load rsCURSOR gNormalCursor)
		(Load rsCURSOR gLoadingCursor)
		(super newRoom: roomNum)
		(if (< argc 2)
			(= gDefaultPicAni (Random 0 5))
		else
			(= gDefaultPicAni picAni)
		)
	)
	
	(method (startRoom roomNum)
		(DisposeLoad
			NULL
			FILEIO_SCRIPT
			JUMP_SCRIPT
			EXTRA_SCRIPT
			WINDOW_SCRIPT
			TIMER_SCRIPT
			FOLLOW_SCRIPT
			REV_SCRIPT
			DCICON_SCRIPT
			DOOR_SCRIPT
			AUTODOOR_SCRIPT
			WANDER_SCRIPT
			AVOID_SCRIPT
			DPATH_SCRIPT
		)
		(DisposeScript DISPOSELOAD_SCRIPT)
		(if gDebugOnExit (= gDebugOnExit FALSE) (SetDebug))
		(gTheSoundFX stop: number: 1)
		(super startRoom: roomNum)
		(if (== gTheSoundFX 11) (gEgo baseSetter: NormalBase))
	)
	
	(method (changeScore addScore)
		(= gScore (+ gScore addScore))
		(if (> addScore 0) (scoreSound playMaybe:))
	)
	
	(method (handleEvent pEvent)
		; ////////////////////////////////////////////////////////////////////
		; ** This is debug functionality                                   //
		; ** Comment it out if you don't want people to cheat in your game //
		; ////////////////////////////////////////////////////////////////////
		(if (== evKEYBOARD (pEvent type?))
			; Check for ALT keys
			(switch (pEvent message?)
				($2f00 (Show 1)) ; alt-v ; Show visual
				($2e00 (Show 4)) ; alt-c ; Show control
				($1900 (Show 2)) ; alt-p ; Show priority
				($3200      ; alt-m
					; Show memory usage
					(ShowFree)
					(FormatPrint
						{Free Heap: %u Bytes\nLargest ptr: %u Bytes\nFreeHunk: %u KBytes\nLargest hunk: %u Bytes}
						(MemoryInfo miFREEHEAP)
						(MemoryInfo miLARGESTPTR)
						(>> (MemoryInfo miFREEHUNK) 6)
						(MemoryInfo miLARGESTHUNK)
					)
				)
				($1400      ; alt-t
					; teleport to room
					(gRoom newRoom: (GetNumber {Which Room Number?}))
				)
				($1700      ; alt-i
					; get inventory
					(gEgo get: (GetNumber {Which inventory#?}))
				)
				($1f00      ; alt-s
					; Show cast
					(gCast eachElementDo: #showSelf)
				)
			)
		)
		; //////////////////////////////////////////////////
		; End of debug functionality                     //
		; //////////////////////////////////////////////////
		(super handleEvent: pEvent)
		(if
		(or (!= (pEvent type?) evSAID) (pEvent claimed?))
			(return TRUE)
		)

; Add global said statements here

		(if (Said 'hi') (Print {Well hello to you too!}))
		(return FALSE)
	)
)


(class Iitem of InvI
	(properties
		said 0
		description 0
		owner 0
		view 0
		loop 0
		cel 0
		script 0
	)
	
	(method (showSelf)
		(Print description #title objectName #icon view loop cel)
	)
)


(instance statusCode of Code
	(properties)
	
	(method (doit param1)
		(Format
			param1
			{ Score: %d of %-3d______________________Template Game_}
			gScore
			gMaxScore
		)
	)
)


(instance ego of Ego
	(properties
		y 1111
		x 0
		z 0
		heading 0
		yStep 2
		view 0
		loop 0
		cel 0
		priority 0
		underBits 0
		signal $2000
		nsTop 0
		nsLeft 0
		nsBottom 0
		nsRight 0
		lsTop 0
		lsLeft 0
		lsBottom 0
		lsRight 0
		brTop 0
		brLeft 0
		brBottom 0
		brRight 0
		cycleSpeed 0
		script 0
		cycler 0
		timer 0
		illegalBits $8000
		xLast 0
		yLast 0
		xStep 3
		moveSpeed 0
		blocks 0
		baseSetter 0
		mover 0
		looper 0
		viewer 0
		avoider 0
		edgeHit 0
	)
)


(instance scoreSound of Sound
	(properties
		state 0
		number SCORE_SOUND
		priority 10
		loop 1
		handle 0
		signal 0
		prevSignal 0
		client 0
		owner 0
	)
)


(instance theMusic of Sound
	(properties
		state 0
		number 1
		priority 0
		loop 1
		handle 0
		signal 0
		prevSignal 0
		client 0
		owner 0
	)
)


(instance theSoundFX of Sound
	(properties
		state 0
		number 1
		priority 5
		loop 1
		handle 0
		signal 0
		prevSignal 0
		client 0
		owner 0
	)
)


(instance theWindow of SysWindow
	(properties)
	
	(method (open)
		(if (< (Graph grGET_COLOURS) 9)
			(if (or (< color 7) (== color 8))
				(= color 0)
				(= back 15)
			else
				(= color 15)
				(= back 0)
			)
		)
		(super open:)
	)
)


(instance NormalBase of Code
	(properties)
	
	(method (doit &tmp temp0)
		(if (== gRoomNumberExit 253)
			(= temp0 22)
		else
			(= temp0 10)
		)
		(gEgo brBottom: (+ (gEgo y?) 1))
		(gEgo brTop: (- (gEgo brBottom?) (gEgo yStep?)))
		(gEgo brLeft: (- (gEgo x?) temp0))
		(gEgo brRight: (+ (gEgo x?) temp0))
	)
)

;
; * THE PUBLIC PROCEDURES
(procedure (SetUpEgo theLoop theView)
	(PlayerControl)
	(gEgo edgeHit: EDGE_NONE)
	(switch argc
		(0
			(SetUpActor gEgo (gEgo loop?) gEgoView)
		)
		(1
			(SetUpActor gEgo theLoop gEgoView)
		)
		(2
			(SetUpActor gEgo theLoop theView)
		)
	)
)


(procedure (SetUpActor pActor theLoop theView)
	(if (> argc 1) (pActor loop: theLoop))
	(if (> argc 2) (pActor view: theView))
	(pActor
		setLoop: -1
		setPri: -1
		setStep: 3 2
		setCycle: Walk
		illegalBits: $8000
		cycleSpeed: 0
		moveSpeed: 0
		ignoreActors: 0
	)
)


(procedure (ProgramControl)
	(User canControl: FALSE canInput: FALSE)
	(gEgo setMotion: NULL)
)


(procedure (PlayerControl)
	(User canControl: TRUE canInput: TRUE)
	(gEgo setMotion: NULL)
)


(procedure (DisposePrintDlg)
	(if gPrintDlg (gPrintDlg dispose:))
)


(procedure (PrintOK)
	(Print {O.K.})
)


(procedure (PrintItIs)
	(Print {It is.})
)


(procedure (PrintYouAre)
	(Print {You are.})
)


(procedure (PrintGoodIdea)
	(Print {Good idea. You might try that again later.})
)


(procedure (PrintNotCloseEnough)
	(Print {You're not close enough.})
)


(procedure (PrintAlreadyTookIt)
	(Print {You already took it.})
)


(procedure (PrintDontHaveIt)
	(Print {You don't have it.})
)


(procedure (PrintCantDoThat mem)
	(if (> (MemoryInfo miLARGESTPTR) mem)
		(return TRUE)
	else
		(Print {You can't do that here; at least, not now.})
		(return FALSE)
	)
)


(procedure (AddViewToPic pView &tmp hView)
	(if pView
		(= hView (View new:))
		(hView
			view: (pView view?)
			loop: (pView loop?)
			cel: (pView cel?)
			priority: (pView priority?)
			posn: (pView x?) (pView y?)
			addToPic:
		)
		(pView posn: (pView x?) (+ 1000 (pView y?)))
	)
)


(procedure (SetInvIOwner index owner &tmp hInvI)
	(= hInvI (gInv at: index))
	(if (< argc 2)
		(hInvI owner: gRoomNumberExit)
	else
		(hInvI owner: owner)
	)
)

(procedure (DebugPrint)
	(if gDebugOut (gDebugOut debugPrint: &rest))
)

;
; * THE INVENTORY ITEMS                                                        *
(instance Nothing of Iitem
	(properties)
)


(instance Test_Object of Iitem
	(properties
		said 'object'
		description {This is a test object in your inventory.}
		owner 0
		view 800
		loop 0
		cel 0
		script 0
		name "Test Object"
	)
)
