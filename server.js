const Hapi = require('@hapi/hapi');
const admin = require('firebase-admin');
const Path = require('path');
const Multer = require('multer');
const bcrypt = require('bcrypt');
const crypto = require('crypto');
const firebase = require('firebase/app');
const firebaseauth = require('firebase/auth');
const { Storage } = require('@google-cloud/storage');
const nodemailer = require('nodemailer');
const fs = require('fs');
const Multer = require('multer');
const Inert = require('@hapi/inert');
const Joi = require('joi');
const Vision = require('@hapi/vision');
const { v4: uuidv4 } = require('uuid');
const jwt = require('jsonwebtoken');
const port = process.env.PORT || 5000;



const storage = new Storage({
  keyFilename: 'soilink.json',
  projectId: 'soilink',
});

const jenisTanahData = require('./tanah.json');

const firebaseConfig = require('./firebaseConfig.json');

admin.initializeApp({
  credential: admin.credential.cert(firebaseConfig),
});

firebase.initializeApp(firebaseConfig);

function generateUniqueToken() {
  const timestamp = new Date().getTime();
  const randomChars = Math.random().toString(36).substring(2, 8);
  return `${timestamp}-${randomChars}`;
};


const init = async () => {
  const server = Hapi.server({
    port: port,
    host: 'localhost',
  });

  await server.register(require('@hapi/inert'));
  await server.register(Vision);

  server.route({
    method: 'GET',
    path: '/',
    handler: (request, h) => {
      return 'Halaman Home';
    },
  });


  server.route({
    method: 'GET',
    path: '/login',
    handler: (request, h) => {
      return h.file(Path.join(__dirname, 'views', 'login.html'));
    },
  });

  server.route({
    method: 'GET',
    path: '/signup',
    handler: (request, h) => {
      return h.file(Path.join(__dirname, 'views', 'signup.html'));
    },
  });

    server.route({
    method: 'GET',
    path: '/profile',
    handler: async (request, h) => {
      try {
        const userRecord = await admin.auth().getUserByEmail(request.auth.credentials.uid);
  
        const userProfile = {
          nama: userRecord.displayName,
          email: userRecord.email,
        };
  
        return { success: true, profile: userProfile };
      } catch (error) {
        console.error('Error fetching user profile:', error.message);
        return h.response({ success: false, message: 'Gagal mengambil profil pengguna' }).code(500);
      }
    },
  });

  server.route({
    method: 'POST',
    path: '/login',
    handler: async (request, h) => {
      try {
        const { email, password } = request.payload;
  
        console.log('Login attempt for email:', email);
  
        const bucket = storage.bucket('soil-bucket');
        const jsonFileName = `History/userHistory-${email}.json`; 
        console.log('Fetching user history from file:', jsonFileName);
        const jsonFileObject = bucket.file(jsonFileName);
        console.log('File :', jsonFileObject);

        let userHistoryFromBucket = { history: [] };
        try {
          const [userHistoryFile] = await jsonFileObject.download();
          userHistoryFromBucket = JSON.parse(userHistoryFile.toString());
          console.log('Fetched user history successfully:', userHistoryFromBucket);
        } catch (error) {
          if (error.code !== 404) {
            console.error('Error fetching user history from Cloud Storage:', error.message);
            return h.response({ success: false, message: 'Error fetching user history' }).code(500);
          }
          console.log('User history file not found for:', email);
        }
  
        if (!userHistoryFromBucket.history) {
          userHistoryFromBucket.history = [];
        }
  
        const userDoc = await admin.firestore().collection('users').doc(email).get();
        const customAuthInfo = userDoc.data()?.customAuthInfo || '';
  
        if (customAuthInfo) {
          if (!password) {
            console.log('Invalid credentials: Password not provided');
            return { success: false, message: 'Invalid credentials: Password not provided' };
          }
  
          const isPasswordValid = await bcrypt.compare(password, customAuthInfo);
  
          if (isPasswordValid) {
            console.log('Password benar');
            if (request.auth && !request.auth.isAuthenticated && request.cookieAuth) {
              request.cookieAuth.set({ uid: email });
            }
  
            const userRecord = await admin.auth().getUserByEmail(email);
  
            return h.response({
              success: true,
              user: userRecord,
              userHistory: userHistoryFromBucket
            }).code(200);
          } else {
            console.log('Invalid credentials');
            return h.response({ success: false, message: 'Invalid Credentials: Wrong Password' }).code(401);
          }
        } else {
          console.log('User does not have customAuthInfo');
          return h.response({ success: false, message: 'Invalid credentials: User not found' }).code(401);
        }
      } catch (error) {
        console.error('Error during login:', error);
        return h.response({ success: false, message: 'Login failed', error: error.message }).code(400);
      }
    },
  });
  
  server.route({
    method: 'POST',
    path: '/signup',
    handler: async (request, h) => {
      const { nama, email, username, password, confirmPassword } = request.payload;

      if (password !== confirmPassword) {
        return { success: false, message: 'Konfirmasi password tidak sesuai' };
      }

      try {
        const hashedPassword = await bcrypt.hash(password, 10); 
        await admin.firestore().collection('users').doc(email).set({
          customAuthInfo: hashedPassword,
        });

        const userRecord = await admin.auth().createUser({
          email,
          password: hashedPassword,
          displayName: nama,
        });

        if (request.auth && !request.auth.isAuthenticated && request.cookieAuth) {
          request.cookieAuth.set({ uid: userRecord.uid });
        }

        return h.response({ success: true, message: 'Signup successful', user: userRecord.toJSON() }).code(201);
      } catch (error) {
        console.error('Error during signup:', error.message);
        return h.response({ success: false, message: 'Signup failed', error: error.message }).code(400);
      }
    },
  });

  server.route({
    method: 'POST',
    path: '/profile',
    handler: async (request, h) => {
      try {
        const { email } = request.payload;
        
        await admin.firestore().collection('users').doc(email).get();
            const userRecord = await admin.auth().getUserByEmail(email);
  
            if (request.auth && !request.auth.isAuthenticated && request.cookieAuth) {
              request.cookieAuth.set({ uid: userRecord.uid });
            }
  
            return h.response({ success: true, message: 'Successfully get the user profile', user: userRecord.toJSON() }).code(200);
      } catch (error) {
        console.error('Error during login:', error);
        return h.response({ success: false, message: 'Failed to get the user profile', error: error.message }).code(401);
      }
    },
  });
  
  server.route({
    method: 'POST',
    path: '/forgot-password',
    handler: async (request, h) => {
      try {
        const { email } = request.payload;
  
        const userRecord = await admin.auth().getUserByEmail(email);
  
        const resetToken = generateUniqueToken();
  
        const expirationTime = new Date();
        expirationTime.setHours(expirationTime.getHours() + 1); 
  
        await admin.firestore().collection('passwordResetTokens').doc(email).set({
          token: resetToken,
          expirationTime: expirationTime,
        });
  
        const resetLink = `http://localhost:3000/reset-password?token=${resetToken}`;
  
        const transporter = nodemailer.createTransport({
          service: 'gmail',
          auth: {
            user: 'soilinkidn@gmail.com',
            pass: 'eucp bkjg cvyq egaq', 
          },
        });
  
        const mailOptions = {
          from: 'soilinkidn@gmail.com',
          to: email,
          subject: 'Forgot Password - Reset Link',
          text: `Click on the following link to reset your password: ${resetLink}`,
        };
  
        const info = await transporter.sendMail(mailOptions);
        console.log('Email sent:', info.response);

        await admin.firestore().collection('passwordResetTokens').doc(email).set({
          token: resetToken,
          expirationTime: expirationTime,
          email: email, 
        });

        return h.response({ success: true, message: 'Reset password link sent successfully.' });
      } catch (error) {
          if (error.code === 'auth/user-not-found') {
          return h.response({ success: false, message: 'Email not found in the system.' }).code(404);
        }
        return h.response({ success: false, message: 'Failed to process forgot password request.' }).code(500);
      }
    },
  });
  
  server.route({
    method: 'GET',
    path: '/reset-password',
    handler: async (request, h) => {
      try {
        const { token } = request.query;
  
        const resetTokenDoc = await admin.firestore().collection('passwordResetTokens').where('token', '==', token).get();
        if (resetTokenDoc.empty) {
          return h.response({ success: false, message: 'The password reset token is invalid or has expired' }).code(400);
        }
  
        const resetTokenData = resetTokenDoc.docs[0].data();
        const expirationTime = resetTokenData.expirationTime.toDate();
  
        if (expirationTime < new Date()) {
          return h.response({ success: false, message: 'Password reset token has expired' }).code(400);
        }
  
        return h.file(Path.join(__dirname, 'views', 'reset-password.html'));
      } catch (error) {
        console.error('Error during reset password page load:', error.message);
        return h.response({ success: false, message: 'Failed to load the password reset page' }).code(500);
      }
    },
  });
  
  server.route({
    method: 'POST',
    path: '/reset-password',
    handler: async (request, h) => {
      try {
        const { token, newPassword, confirmNewPassword } = request.payload;
  
        const resetTokenDoc = await admin.firestore().collection('passwordResetTokens').where('token', '==', token).get();
        if (resetTokenDoc.empty) {
          return h.response({ success: false, message: 'The password reset token is invalid or has expired' }).code(400);
        }
  
        const resetTokenData = resetTokenDoc.docs[0].data();
        const expirationTime = resetTokenData.expirationTime.toDate();
        const email = resetTokenData.email;
  
        if (expirationTime < new Date()) {
          return h.response({ success: false, message: 'Password reset token has expired' }).code(400);
        }
  
        if (newPassword === confirmNewPassword) {
          const hashedNewPassword = await bcrypt.hash(newPassword, 10);
          
          console.log('Email:', email);
          const userDoc = await admin.firestore().collection('users').doc(email).get();
          await admin.firestore().collection('users').doc(email).set({
            customAuthInfo: hashedNewPassword,
          }, { merge: true }); 
        
          await admin.firestore().collection('passwordResetTokens').doc(email).delete();
  
          return h.response({ success: true, message: 'Password is successfully reset' }).code(200);
        } else {
          return h.response({ success: false, message: 'Confirmation of new password is not correct' }).code(400);
        }
      } catch (error) {
        console.error('Error during password reset:', error.message);
        return h.response({ success: false, message: 'Password reset failed', error: error.message }).code(400);
      }
    },
  });

    server.route({
    method: 'POST',
    path: '/upload',
    options: {
      payload: {
          maxBytes: 10485760,
          output: 'stream',
          parse: true,
          allow: 'multipart/form-data',
          multipart: true,
      },
      validate: {
          payload: Joi.object({
              file: Joi.any()
                  .meta({ swaggerType: 'file' })
                  .required()
                  .description('Image file'),
          }),
      },
    },
    handler: async (request, h) => {
        try {
            const file = request.payload.file;
            const fileName = file.hapi.filename;
            const fileBuffer = file._data;

            console.log('Received file:', fileName);

            const bucket = storage.bucket('soil-bucket');
            const imageName = `Images/${fileName}`;
            const fileObject = bucket.file(imageName);


            await fileObject.save(fileBuffer);

            console.log('File saved to Cloud Storage:', fileName);

            return { success: true, message: 'File uploaded successfully' };
        } catch (error) {
            if (error instanceof Multer.MulterError) {
                console.error('Multer error:', error.message);
                return h.response({ success: false, message: 'Error uploading file', error: error.message }).code(400);
            } else {
                console.error('Internal server error:', error);
                return h.response({ success: false, message: 'Internal Server Error' }).code(500);
            }
        }
    },
  });

  server.route({
    method: 'GET',
    path: '/images/{filename}',
    handler: (request, h) => {
        const fileName = request.params.filename;

        const bucket = storage.bucket('soil-bucket'); 
        const file = bucket.file(fileName);

        return file.createReadStream().pipe(h.response(file.createReadStream()).type('image/jpeg'));
    },
  });

  server.route({
    method: 'GET',
    path: '/jenis-tanah',
    handler: (request, h) => {
      try {
        const jenisTanahList = jenisTanahData.jenisTanah.map(jenisTanahItem => ({
          nama: jenisTanahItem.nama,
          deskripsi: jenisTanahItem.deskripsi,
          gambar: `https://storage.googleapis.com/soil-bucket/gambar_tanah/${jenisTanahItem.gambar}`,
        }));
  
        return h.response(jenisTanahList);
      } catch (error) {
        console.error('Error handling GET jenis tanah:', error.message);
        return h.response({ success: false, message: 'Internal Server Error' }).code(500);
      }
    },
  });
  
  server.route({
    method: 'GET',
    path: '/jenis-tanah/{jenis}/gambar',
    handler: (request, h) => {
      try {
        const requestedJenis = request.params.jenis.toLowerCase();
        const jenisTanahItem = jenisTanahData.jenisTanah.find(
          (jenis) => jenis.nama.toLowerCase() === requestedJenis
        );
  
        if (jenisTanahItem && jenisTanahItem.gambar) {
          const publicURL = `https://storage.googleapis.com/soil-bucket/gambar_tanah/${jenisTanahItem.gambar}`;
        
        let contentType = 'image/jpeg';
        if (jenisTanahItem.gambar.endsWith('.png')) {
          contentType = 'image/png';
        }

        return h.response().redirect(publicURL).type(contentType);
        } else {
          return h
            .response({
              success: false,
              message: 'Jenis tanah atau gambar tidak ditemukan',
            })
            .code(404);
        }
      } catch (error) {
        console.error('Error handling GET gambar:', error.message);
        return h
          .response({
            success: false,
            message: 'Internal Server Error',
          })
          .code(500);
      }
    },
  });
  
  
  server.route({
    method: 'POST',
    path: '/add-to-history',
    options: {
      payload: {
        maxBytes: 10485760,
        output: 'stream',
        parse: true,
        allow: 'multipart/form-data',
        multipart: true,
      },
      validate: {
        payload: Joi.object({
          email: Joi.string().email().required().description('User email'),
          soil_type: Joi.string().allow('').default('').description('Soil type'),
          description: Joi.string().allow('').default('').description('Description'),
          note: Joi.string().allow('').default('').description('Note'),
          date_time: Joi.string().allow('').default('').description('Date and time'),
          lat: Joi.string().allow('').default('').description('Latitude'),
          long: Joi.string().allow('').default('').description('Longitude'),
          file: Joi.any()
            .meta({ swaggerType: 'file' })
            .allow('')
            .default('')
            .description('Image file'),
        }),
      },
    },
    handler: async (request, h) => {
      try {
        const { email, file, soil_type, description, note, date_time, lat, long } = request.payload;

        let publicUrl = '';
        let userHistory = {};

        if (file) {
          const authId = generateAuthId();

          const fileName = `images/${uuidv4()}-${file.hapi.filename}`;
          const bucket = storage.bucket('soil-bucket');
          const fileObject = bucket.file(fileName);
          const fileBuffer = file._data;

          await fileObject.save(fileBuffer, { contentType: file.hapi.headers['content-type'] });

          publicUrl = `https://storage.googleapis.com/${bucket.name}/${fileObject.name}`;
        }

        const jsonFileName = `History/userHistory-${email}.json`;
        const bucket = storage.bucket('soil-bucket');
        const jsonFileObject = bucket.file(jsonFileName);

        try {
          const [userHistoryFile] = await jsonFileObject.download();

          userHistory = JSON.parse(userHistoryFile.toString());
        } catch (error) {
          if (error.code === 404) {
            userHistory = { [email]: { history: [] } };
          } else {
            console.error('Error fetching user history from Cloud Storage:', error.message);
            return h.response({ success: false, message: 'Error fetching user history' }).code(500);
          }
        }
        userHistory[email] = userHistory[email] || { history: [] };

        userHistory[email].history.push({
          id: generateAuthId(),
          image: publicUrl || '', 
          soil_type: soil_type || '',
          description: description || '',
          note: note || '',
          date_time: date_time || '',
          lat: lat || '',
          long: long || '',
        });

        const userHistoryJson = JSON.stringify(userHistory, null, 2);

        await jsonFileObject.save(userHistoryJson, { contentType: 'application/json' });

        return {
          success: true,
          message: 'Data added to user history successfully',
          userHistory,
        };
      } catch (error) {
        console.error('Error during adding to history:', error.message);
        return h
          .response({
            success: false,
            message: 'Failed to add data to user history.',
            error: error.message,
          })
          .code(500);
      }
    },
  });
  
  function generateAuthId() {
    return uuidv4();
  }

  server.route({
    method: 'GET',
    path: '/get-history/{email}',
    handler: async (request, h) => {
      try {
        const { email } = request.params;
  
        const bucket = storage.bucket('soil-bucket');
        const jsonFileName = `History/userHistory-${email}.json`;
        const jsonFileObject = bucket.file(jsonFileName);
  
        let userHistory = {};
  
        try {
          const [userHistoryFile] = await jsonFileObject.download();
  
          userHistory = JSON.parse(userHistoryFile.toString());
        } catch (error) {
          if (error.code === 404) {
            return h.response({ success: false, message: 'User history not found' }).code(404);
          } else {
            console.error('Error fetching user history from Cloud Storage:', error.message);
            return h.response({ success: false, message: 'Error fetching user history' }).code(500);
          }
        }
  
        return { success: true, userHistory };
      } catch (error) {
        console.error('Error during fetching user history:', error.message);
        return h.response({ success: false, message: 'Failed to fetch user history.', error: error.message }).code(500);
      }
    },
  });
  

  server.route({
    method: '*',
    path: '/{any*}',
    handler: (request, h) => {
      return h.response({ success: false, message: 'Route not found' }).code(404);
    },
  });

  server.ext('onPreResponse', (request, h) => {
    const response = request.response;
    if (response.isBoom) {
      console.error('Error:', response.message);
      return h.response({ success: false, message: 'An internal error occurred' }).code(500);
    }
    return h.continue;
  });

  await server.start();
  console.log('Server running on', server.info.uri);
};

process.on('unhandledRejection', (err) => {
  console.log(err);
  process.exit(1);
});

init();

