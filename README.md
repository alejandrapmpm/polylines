# Robot Application

This application is intented to simuate the movement of a robot along a polyline.  
A polyline is a set of points (latitud and longitud) that are compressed using a compression algorithm.  
The robot should move every 1-3 meters/second along a polyline. Every 15 minutes should output a report of the particles read until that moment.
The report looks like:

```json
{"timestamp":1588766610723,"location":{"lat":51.502659365074685,"lng":-0.14119854637849288},"level":"Moderate","source":"ROBOT"}
```

## How to run the program

Give the jar run the following command:

```json
java -jar polylines-1.0-jar-with-dependencies.jar 'mpjyHx`i@VjAVKnAh@BHHX@LZR@Bj@Ml@WWc@]w@bAyAfBmCb@o@pLeQfCsDVa@@ODQR}AJ{A?{BGuAD_@FKb@MTUX]Le@^kBVcAVo@Ta@|EaFh@m@FWaA{DCo@q@mCm@cC{A_GWeA}@sGSeAcA_EOSMa@}A_GsAwFkAiEoAaFaBoEGo@]_AIWW{AQyAUyBQqAI_BFkEd@aHZcDlAyJLaBPqDDeD?mBEiA}@F]yKWqGSkICmCIeZIuZi@_Sw@{WgAoXS{DOcAWq@KQGIFQDGn@Y`@MJEFIHyAVQVOJGHgFRJBBCCSKBcAKoACyA?m@^yVJmLJ{FGGWq@e@eBIe@Ei@?q@Bk@Hs@Le@Rk@gCuIkJcZsDwLd@g@Oe@o@mB{BgHQYq@qBQYOMSMGBUBGCYc@E_@H]DWJST?JFFHBDNBJ?LED?LBv@WfAc@@EDGNK|@e@hAa@`Bk@b@OEk@Go@IeACoA@a@PyB`@yDDc@e@K{Bi@oA_@w@]m@_@]QkBoAwC{BmAeAo@s@uAoB_AaBmAwCa@mAo@iCgAwFg@iDq@}G[uEU_GBuP@cICmA?eI?qCB{FBkCI}BOyCMiAGcAC{AN{YFqD^}FR}CNu@JcAHu@b@_E`@}DVsB^mBTsAQKkCmAg@[YQOIOvAi@[m@e@s@g@GKCKAEJIn@g@GYGIc@ScBoAf@{A`@uAlBfAG`@'
```

## How to build the project

Just run  ` mvn clean install ` and you will run the build and generate the artifacts. 
The build also generates a Jacoco report for coverage.

## Considerations:

- The robot's speed is currently static. It is set to 2 m/s.
- Please note that I'm not handling invalid polylines yet
