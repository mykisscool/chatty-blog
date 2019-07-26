# Chatty Blog

_Chatty Blog_ is a simple and interactive blogging application that provides immediate feedback on comments made to posts & other comments in an organized & non-disruptive manner using Akka Streams and WebSockets on the Play Framework.

[**DEMO** -- Start chatting right now!](https://chatty-blog.herokuapp.com/)

## Logins

| Name           | Username         | Password    |
|----------------|------------------|-------------|
| Samus Aran     | cold_as_ice_beam | password123 |
| Mega Man       | master_blaster   | password321 |
| Simon Belmont  | whip_it_good     | password456 |
| Kid Icarus     | mr_pit           | password654 |
| Glass Joe      | one_99           | password789 |
| Princess Zelda | so_sleepy        | password987 |
| Donkey Kong    | banana_slamma    | password012 |
| Mario Mario    | its_a_me         | password210 |

## Screenshots

> *Posts page*

![Screenshot 1](/public/images/screenshots/screenshot-1.png?raw=true "Posts page")

---

> *Post page with notifications*

![Screenshot 2](/public/images/screenshots/screenshot-2.png?raw=true "Post page with notifications")

## Notes

+ Be patient- this app is probably sleeping (it's not slow). It's hosted on Heroku's free dyno plan and falls asleep after an hour of inactivity.

+ The max age of a user session is set to 30 minutes- that should be ample time to get a feel for this blog's features & reactivity.

+ The Akka ActorSystem periodically purges user comments on the off chance this project is discovered and gets inundated with activity. Please keep it clean. For the children.

+ This is a fun concept meant to demonstrate reactive capabilities on a blog. There are some minor areas of improvement:

  + Improve management of multiple comment notifications.
  
  + Consider immediately injecting comments into the DOM. 
  
  + Improve logging, error handling, and testing.
