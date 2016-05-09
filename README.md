Android JobScheduler Example
============================

This project is a very simple example of using the JobScheduler as an alternative to the AlarmService for scheduling background (usually background, but not necessarily, I guess) work some time in the future. The UI is simple and bare-bones. There are slides included in the repo that are built to guide a discussion within a group about the example.

Setup and Dependencies
----------------------

This project is built using AS 2.1, Gradle plugin 2.1 and Gradle Wrapper 2.10. As long as you have a sufficient version of AS installed and use the Gradle wrapper everything should work just fine. If you keep a local install of Gradle and use it for command-line work then you will either need to have a sufficient version of Gradle installed or tweak this setup to match your needs.

| Name                 | Provider    | Summary |
|:--------------------:|:-----------:|:-------:|
| J-Unit               |             | Support for java unit testing |
| Android Appcompat v7 | Google      | Backwards compatibility library |
| ButterKnife | Jake Wharton      | View Injection library |
| RxJava | ReactiveX      | Reactive Extensions for Java thread work |
| RxAndroid | ReactiveX      | Android Schedulers for Reactive Extensions |

Background and Information
--------------------------

From time to time we find the need to schedule a task (usually a background task) some time in the future -- we may want to perform a background data sync or do some background grinding on some data, for example. In reality we would like to schedule it some time after a threshold delay, but would prefer it not to start until the device is in an acceptable state. For example, we may prefer the device is idle, or we may prefer that we have network connectivity. Or, since a particular task is going to download a lot of data we may prefer the device is connected to a non-metered network (wifi rather than cell).

I have seen a variety of ways to accomplish this. Not knowing any better, a person may kick off an IntentService that imposes a hard `Thread.sleep()`. Or if it will result in some UI activity and therefore need to return to the main thread I have seen a pattern where the background thread posts to a `Handler` with a delay. This method has several drawbacks, not the least of which is that the app must remain alive for the duration of the delay. If the app is destroyed we lose the opportunity to perform our work. If the app is not destroyed it continues to tie up resources in the mean time. In particular, this method is not desirable for the long delays we would expect for this type of use case.

The preferred method until API 21 was to use the `AlarmService` from Android. With this approach we schedule an alarm with the Android framework to trigger after a given period of time. We have a few options with this method, including the opportunity to wake the device up if it's asleep. With this method we create an `Intent` (probably to a `Serivce` or `IntentService`) to perform the work, and that `Intent` is wrapped in a `PendingIntnent` for the future execution. This method works well, and if you have reason not to use classes from newer APIs it will be adequate. But it still has a few drawbacks. In particular, you do not have any control over the state of the device when the alarm triggers (or, rather, whether to wait until the state of the device is more desirable). Also, if the job gets interrupted, the developer must create a new alarm to try again.

Finally, we come to the `JobScheduler` class. `JobScheduler` allows the developer to not only schedule a task for some time in the future, but it allows us to put additional constraints on when the job gets started. As noted above, we have the ability to specify that the device must be idle and/or meet certain network criteria. This means that the job will run **_no sooner_** than the time threshold, but after that threshold it will only run when the other preconditions are met. **_In addition, if those other preconditions become violated during execution of the job, it will be stopped._** When this happens Android calls a method that requires a `boolean` return: "Should we reschedule this job when the conditions are met again?". That means that if we depend on network connectivity, but lose the network, we will have the opportunity to mark our progress and pick up again when we are connected again. It's pretty powerful. 

However, there are a couple of drawbacks (they're small; no software is perfect, so it goes without saying that anything has a couple of drawback. But here they are anyway). We only have access to `JobScheduler` in API level 21 and up, so your ability to use it may still be limited. You can always switch your implementation at run time based on API level by providing a couple of methods to call conditionally. Another drawback is that it requires a little more infrastructure. You can't just set a `PendingIntent` to kick off an already-existing `Service`, for example. The `JobScheduler` requires you to impelement a `JobService` on the other side. It's a very minor thing -- one more class in your application -- and that `JobSerivce` is the thing that allows us easily reschedule the job.

*NOTE: There is a 3rd party library for a `JobSchedulerCompat` that will allow you to use `JobScheduler` for pre-API 21, but I do not use that in this example and leave it to the developer to research that option.*

The code should be reasonably documented to explain the whys and hows, etc. But, as mentioned above, no code is perfect (not even mine). Please let me know if I have left something out.