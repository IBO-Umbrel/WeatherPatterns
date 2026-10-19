# Practice 6 answers and notes

The project uses the same repository and Compose screen for all four patterns.
The implementations follow the assignment examples, including independent requests
for each button press.

## Question 1

Simplicity is lost because finding the form validation means searching through
network, storage and UI code. Testing is harder because testing validation also
requires dealing with an Activity and its dependencies. Maintenance costs more
because changing the server response can accidentally affect display or storage.
I would first move the server call and response handling into a data layer,
because they have a clear responsibility and can be tested separately.

## Question 2

1. Understand what the existing app does and what requirements it has.
2. Identify the responsibilities currently mixed together.
3. Learn the roles and limitations of a suitable pattern.
4. Apply it to one small feature and test the result.
5. Use the experience to improve other features, choosing their structure as needed.

"Use MVVM everywhere" ignores the requirements and the developer's understanding.
No single pattern suits every project, and modules in one app can use different structures.

## Question 3

WeatherRepository catches IllegalArgumentException and returns Result.failure.
Cancellation still propagates. WeatherRepositoryTest covers known and unknown
cities and also checks cancellation.

## Question 4

MainActivity opens MVC, MVP, MVVM and MVI. All four Activities are registered in
AndroidManifest.xml. The shared screen contains the four city buttons.

## Question 5

1. Weather: Model.
2. WeatherRepository: Model.
3. WeatherScreen: View.
4. The onClick lambda: Controller and View, because it both loads data and updates UI.
5. FakeWeatherApi: Model.
6. WeatherController: Controller.

Mixing Controller and View makes the Activity larger and ties logic tests to
Android UI code. The Model has no knowledge of the View.

## Question 6

The Controller starts loading, asks the View to render, loads the city, and asks
the View to render again. The comment above MvcActivity.render explains that the
View reads the Model directly. Passing Weather would move data delivery to the
Controller and remove that direct read for the forecast.

## Question 7

(A) breaks the rule that the View cannot access the Model.
(B) breaks the passive View rule by deciding the result instead of letting the
Presenter decide it.
(C) and (D) couple the Presenter to a concrete Activity and TextView, making a
plain fake View impossible to substitute in a local test. They still respect
the one-Presenter-to-one-View relationship. Also, the displayed snippet cannot
access private statusLabel from a separate class.

The constructor should accept an interface:

```kotlin
class WeatherPresenter(private val view: WeatherContract.View)
```

## Questions 8 and 9

The Presenter reports loading, then weather or an error through WeatherContract.View.
destroy() cancels its coroutine scope, and MvpActivity calls it in onDestroy().
The tests check success, failure, two requests, and cancellation of a delayed callback.

The following rotation results are expected from the code, not recorded emulator
observations. Actual Activity hash codes must be obtained from Logcat.

Before cancellation was added, the old Presenter would keep the old Activity
reference and call showWeather on that destroyed instance. After the fix, its
request is cancelled and no delayed result reaches that Activity.
The new Activity shows "Pick a city" because both its status and Presenter are
created again. To retain the state and work during rotation, they need to live
in a retained owner such as a ViewModel.

Manual experiment: open MVP, filter Logcat with tag:MVP, select Bukhara, and rotate
within three seconds. Record the old onCreate/onDestroy hash and the new onCreate
hash. The fixed version should not log showWeather for the destroyed Activity.
A before-fix comparison requires temporarily omitting presenter.destroy(), then
restoring it after the experiment.

## Question 10

| Package | Classes |
| --- | --- |
| data/api | WeatherApi, FakeWeatherApi |
| data/model | Weather |
| data/repository | WeatherRepository |
| presentation/viewmodel | WeatherViewModel, WeatherUiState |
| presentation/view | WeatherScreen, MvvmActivity |
| di | WeatherModule |

The project names the MVVM presentation packages mvvm/viewmodel and mvvm/view,
and shares WeatherScreen in ui, following the assignment project map.

A ViewModel must not retain an Activity or composable because the View can be
destroyed while the ViewModel survives. It exposes state instead. A local test
can select Atlantis and check Error without creating an Activity.

## Question 11

WeatherViewModel exposes StateFlow and uses viewModelScope.
Expected rotation result: the new Activity observes Loading while the original
request continues, then shows "Bukhara: 27°C, Windy". If the request has already
finished, it shows that result immediately. Rotation does not lose the request.
This describes configuration changes, not process death.

## Question 12

(1) breaks immutability: separate public mutable fields can change independently
and do not form one immutable state value. Unidirectional flow is also at risk,
but the mutable state declaration is the immediate issue.
(2) breaks separation of concerns: the API is tied to particular widgets rather
than expressing user intentions.
(3) breaks immutability: var properties allow an existing state to change.
(4) breaks unidirectional flow: the View directly changes state instead of
sending an intent. It also crosses the state ownership boundary.

The required declarations are:

```kotlin
data class WeatherState(
    val isLoading: Boolean = false,
    val weather: Weather? = null,
    val error: String? = null,
)

sealed interface WeatherIntent
{
    data class SelectCity(val city: String) : WeatherIntent
    data object DismissError : WeatherIntent
}

// Public function in WeatherMviViewModel:
fun onIntent(intent: WeatherIntent)
```

## Question 13

WeatherMviViewModel updates one immutable state with copy. Loading is checked
before error and weather so an older forecast cannot hide a new request.
DismissError clears only the error and retains the earlier weather.
WeatherMviViewModelTest covers success, failure, and dismissing an error while
keeping the previous weather. As in the supplied screen, DismissError is tested
through the ViewModel; no extra screen button is added.

## Question 14

The import columns below refer to the logic class, not the entire package.
Every pattern's Activity imports Android and AndroidX.

| Pattern and logic class | Imports android. | Imports androidx. | View references | What survives rotation |
| --- | --- | --- | --- | --- |
| MVC: WeatherController | No | No | Controller and Model | Shared repository; old controller work also continues, but the new screen has a new Model |
| MVP: WeatherPresenter | No | No | Presenter | Shared repository; old request is cancelled and status resets |
| MVVM: WeatherViewModel | No | Yes, lifecycle | ViewModel | ViewModel, state, and in-flight request |
| MVI: WeatherMviViewModel | No | Yes, lifecycle | ViewModel | ViewModel, immutable state, and in-flight request |

MVC: Controller/Model tests can use JUnit, an injected Unconfined scope and a
zero-latency fake without kotlinx-coroutines-test.
MVP: Basic Presenter tests can do the same with a fake View. The additional
delayed cancellation test uses kotlinx-coroutines-test to advance virtual time.
MVVM: ViewModel tests need kotlinx-coroutines-test to replace Dispatchers.Main.
MVI: ViewModel tests also need kotlinx-coroutines-test; pure state-value checks do not.

MVC deliberately keeps the supplied Controller lifetime to show its limitation:
its independent scope can finish against the old View after rotation.
The comparison concerns Activity recreation, not persistence after process death.

## Question 15

1. About screen: no presentation pattern is necessary for two static texts.
   This follows the idea that requirements determine the structure. The cost is
   that growing behavior may require refactoring later.
2. Chat: MVI gives one state for messages, typing and connection status.
   This applies a pattern to the feature's requirements. The cost is extra
   intent and state-update code.
3. Legacy app: move database access into a shared data layer and migrate one
   feature at a time to MVP. Interfaces allow logic to move out of Activities
   without rewriting every screen. This follows gradual understanding and
   module-specific structure. The cost is temporary inconsistency and extra
   interfaces during migration.

The About and chat screens can use different structures in the same app.
The lecture explicitly allows modules within one complex project to have their
own structure.

## Verification

Gradle testDebugUnitTest and assembleDebug both passed. All 11 local tests passed
(10 weather tests and the existing example test). The debug APK is at
app/build/outputs/apk/debug/app-debug.apk.
No emulator or device was connected, so navigation, visual checks and actual
rotation/Logcat observations remain unverified. The rotation descriptions above
are expected behavior, not fabricated experiment results.
