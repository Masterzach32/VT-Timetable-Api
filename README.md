# VT Timetable Api

A Kotlin API for the [Virginia Tech Timetable of Classes](https://banweb.banner.vt.edu/ssb/prod/HZSKVTSC.P_DispRequest)

Used by the course opening notifier [Course Patrol](https://coursepatrol.net)!

### Usage:

The API provides access to the timetable through the Timetable object. 

The Timetable object provides a few useful methods for looking up class sections:
```kotlin
// will pull the class with crn 17206 (physics lab) from the Spring 2018 semester if the section is open.
val section = Timetable.lookupCrn("17206", term = Term(Session.SPRING, "2018"), openOnly = true)
```

Both the `term` and `openOnly` parameters are optional, defaulting to the current term and pulling only open sections.

Another method available is the `lookupCourse` method, which can pull sections based off of the department and class number.
```kotlin
// will pull all (including full) statics classes for the current term.
val sections = Timetable.lookupCourse(sectionCode = "ESM", classNumber = "2104")
```

You can also search for class sections by CLE (or pathways).
```kotlin
val sections = Timetable.lookupCle(area = Curriculum.CLE.AREA_2, term = Term("201909"))
// or
val sections = Timetable.lookupCle(area = Curriculum.Pathway.PATHWAY_2, term = Term("201909"))
```

If you want to fine tune your search, all available inputs can be used on the `lookup` method:
```kotlin
val sections = Timetable.lookup(...)
```
