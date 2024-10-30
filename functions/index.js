/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */


const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendRideCancelNotification = functions.database.ref("/Rides/{rideId}/status")
    .onUpdate((change, context) => {
      const newStatus = change.after.val();
      const rideId = context.params.rideId;

      if (newStatus === "canceled") {
        return admin.database().ref(`/Rides/${rideId}`).once("value")
            .then((rideSnapshot) => {
              const ride = rideSnapshot.val();
              const driverId = ride.driverId;

              const payload = {
                  notification: {
                    title: "Ride Canceled",
                    body: `Your ride from ${ride.origin} to ${ride.destination} has been canceled by the driver.`,
                  },
                  data: {
                    rideId: rideId,
                    origin: ride.origin,
                    destination: ride.destination,
                  },
                };

              // Send notification to all customers who booked the ride
              const bookingPromises = Object.keys(ride.bookings || {}).map((bookingId) => {
                const booking = ride.bookings[bookingId];
                const customerId = booking.customerId;

                const message = {
                  topic: `customer_${customerId}`,
                  ...payload,
                };

                return admin.messaging().sendToTopic(`customer_${customerId}`, payload);
              });

              return Promise.all(bookingPromises);
            });
      } else {
        return null;
      }
    });

exports.sendBookingStatusUpdateNotification = functions.database.ref("/Bookings/{bookingId}/status")
    .onUpdate((change, context) => {
      const newStatus = change.after.val();
      const bookingId = context.params.bookingId;

      return admin.database().ref(`/Bookings/${bookingId}`).once("value")
          .then((bookingSnapshot) => {
            const booking = bookingSnapshot.val();
            const rideId = booking.rideId;
            const customerId = booking.customerId;

            let notificationBody;

            if (newStatus === "accepted") {
              notificationBody = "Your booking has been accepted by the driver.";
            } else if (newStatus === "canceled") {
              notificationBody = "Your booking has been declined by the driver.";
            } else if (newStatus === "rideCanceled") {
                notificationBody = "Your ride has been canceled by the driver.";
            } else {
              return null; // If the status is not accepted or canceled, do nothing
            }

            const payload = {
              notification: {
                title: "Booking Status Update",
                body: notificationBody,
              },
              data: {
                bookingId: bookingId,
                rideId: rideId,
              },
            };

            const message = {
              topic: `customer_${customerId}`,
              ...payload,
            };

            return admin.messaging().send(message)
              .then((response) => {
                console.log("Successfully sent message:", response);
              })
              .catch((error) => {
                console.error("Error sending message:", error);
              });
          });
    });
exports.sendNewBookingNotification = functions.database.ref("/Bookings/{bookingId}")
    .onCreate((snapshot, context) => {
      const booking = snapshot.val();
      const bookingId = context.params.bookingId;
      const rideId = booking.rideId;

      return admin.database().ref(`/Rides/${rideId}`).once("value")
          .then((rideSnapshot) => {
            const ride = rideSnapshot.val();
            const driverId = ride.driverId;

            const payload = {
              notification: {
                title: "New Booking Request",
                body: `You have a new booking request for the ride from ${ride.origin} to ${ride.destination}.`,
              },
              data: {
                rideId: rideId,
                bookingId: bookingId,
              },
            };

            const message = {
              topic: `driver_${driverId}`,
              ...payload,
            };

            return admin.messaging().send(message)
              .then((response) => {
                console.log("Successfully sent message:", response);
              })
              .catch((error) => {
                console.error("Error sending message:", error);
              });
          });
    });