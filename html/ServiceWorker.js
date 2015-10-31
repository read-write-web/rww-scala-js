/**
 * Created by hjs on 29/10/2015.
 * This needs to be in the root or else I won't get
 * access rights to the scala-js lib below
 * This suggests creating another js application and placing it in the target.
 */

importScripts("rww-scala-js-fastopt.js")
rww.auth.ServiceWorkerAuth().run(this)