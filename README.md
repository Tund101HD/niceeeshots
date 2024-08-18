# NiceeShots

### 3rd place submission for the Hackathon "Cavehack" over at https://discord.gg/coder-s-cove-1042720184391172159

The idea of NiceeShotss was to create an app, that motivates people to post images they take on a daily basis
thus practicing and sharpening their skills more and more. The core concept is similar to Pokemon Go, where 
players can add PokeStops and Arenas on a map grid and are motivated to go outside and adventure through the 
world. NiceeShotss' idea was, that every user can upload a picture from their gallery or a picture taken right 
at the spot, for people to then see on their map and get the chance to comment, refine and post their own picture.
All while NiceeShotss would make sure to keep track of important data like the focal lenght, orientation, ISO, 
exposure time, etc. that make the image look like it does, for learners to copy and follow along. The core idea
was also centered around a leveling system, in which you level up through posting pictures and finishing tasks. The
The user receives Tasks that dictate what object to photograph, which then gets fed thorough OpenAis Image recognition
to check if the task was successfully finished. 

All of this was to satisfy one of Covehacks three themes:
```
Gamification of Learning
    - Make learning fun, interactive, and engaging for all ages.
    - Incorporate game mechanics like point scoring, badges, and leaderboards.
    - Design apps that reward progress and make learning feel like an adventure.
    - Create educational tools that are effective and enjoyable.
```
As of now, a lot of the code handling the display of pictures is not in this repository, as I have removed the not working
parts to not accidentally crash during the live showcase. All the code in this repository is, what the finished submission
was.  


## Technologies

**Core**
> NiceeShotss is written in Kotlin and all the ui-elements are also kotlin native, with the build in setContentView.

**Database**
> UserStorage and the Storage of images is handled through Google's Firebase. 

**MapView**
> The map on the MainActivity is created using Matbox, an alternative to Google Maps.

Note that prior to this Hackathon I have never ever touched neither Firebase, Mapbox nor Kotlin in general. So naturally 
the entire codebase is an complete atrocity and there's definitely better coding practices. (24h time didn't help lol)


## Future

I have not yet decided if I want to keep going with the app, just to see where it could be going. I might start
a YouTube series, who knows. In the meantime, if anyone wants to contribute for some reason feel free to do so.



## More info about the Hackathon
> https://matthewtrent.me/covehack/summer-2024

### A big thank you to the judges of this event for making it possible that I could be part of this adventure and I hope that this is only the beginning of the Hackathons in Coder's Cove