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
const smtpTransport = require('nodemailer-smtp-transport');
const { OAuth2Client } = require('google-auth-library');
const googleClientId = '985911723534-nt20v2sijh9s2e9qnbjfv36dpgkh9q5c.apps.googleusercontent.com';
const googleClientSecret = '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';
const port = process.env.PORT || 3000;

const client = new OAuth2Client({
  clientId: googleClientId,
  clientSecret: googleClientSecret,
});


const storage = new Storage({
  keyFilename: 'soilink.json', // Ganti dengan path file kredensial Anda
  projectId: 'soilink',
});

const bucket = storage.bucket('staging.soilink.appspot.com');

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
  const storageConfig = Multer.memoryStorage();
  const upload = Multer({ storage: storageConfig });

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

  // server.route({
  //   method: 'GET',
  //   path: '/login',
  //   handler: (request, h) => {
  //     try {
  //       // Logika autentikasi, jika diperlukan
        
  //       // Jika autentikasi berhasil
  //       return h.response({ success: true }).code(200);
        
  //       // Jika autentikasi gagal
  //       // return h.response({ success: false, message: 'Autentikasi gagal' }).code(401);
        
  //     } catch (error) {
  //       console.error(error);
  //       return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
  //     }
  //   },
  // });
  
  // server.route({
  //   method: 'GET',
  //   path: '/signup',
  //   handler: (request, h) => {
  //     try {
  //       // Logika pendaftaran, jika diperlukan
        
  //       // Jika pendaftaran berhasil
  //       return h.response({ success: true }).code(200);
        
  //       // Jika pendaftaran gagal
  //       // return h.response({ success: false, message: 'Pendaftaran gagal' }).code(403);
        
  //     } catch (error) {
  //       console.error(error);
  //       return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
  //     }
  //   },
  // });

  // server.route({
  //   method: 'GET',
  //   path: '/login',
  //   handler: async (request, h) => {
  //     try {
  //       // Logika autentikasi, jika diperlukan
        
  //       // Contoh autentikasi yang selalu menghasilkan kesalahan
  //       throw new Error('Autentikasi gagal');
        
  //       // Jika autentikasi berhasil
  //       return h.response({ success: true }).code(200);
        
  //     } catch (error) {
  //       console.error(error);
  
  //       // Catat detail kesalahan ke log file atau sistem pelacakan kesalahan
  //       // fs.appendFileSync('error.log', `${new Date().toISOString()}: ${error.message}\n`);
  
  //       // Kembalikan respons HTTP dengan pesan kesalahan yang umum
  //       return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
  //     }
  //   },
  // });
  
  // server.route({
  //   method: 'GET',
  //   path: '/signup',
  //   handler: async (request, h) => {
  //     try {
  //       // Logika pendaftaran, jika diperlukan
        
  //       // Contoh pendaftaran yang selalu menghasilkan kesalahan
  //       throw new Error('Pendaftaran gagal');
        
  //       // Jika pendaftaran berhasil
  //       return h.response({ success: true }).code(200);
        
  //     } catch (error) {
  //       console.error(error);
  
  //       // Catat detail kesalahan ke log file atau sistem pelacakan kesalahan
  //       // fs.appendFileSync('error.log', `${new Date().toISOString()}: ${error.message}\n`);
  
  //       // Kembalikan respons HTTP dengan pesan kesalahan yang umum
  //       return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
  //     }
  //   },
  // });

  server.route({
    method: 'POST',
    path: '/login',
    handler: async (request, h) => {
      try {
        const { email, password } = request.payload;
  
        console.log('Login attempt for email:', email);
  
        // Dapatkan informasi khusus untuk otentikasi dari Firestore
        const userDoc = await admin.firestore().collection('users').doc(email).get();
        const customAuthInfo = userDoc.data()?.customAuthInfo || '';
  
        if (customAuthInfo) {
          // Bandingkan password yang diberikan dengan informasi khusus
          const isPasswordValid = await bcrypt.compare(password, customAuthInfo);
  
          if (isPasswordValid) {
            console.log('Password benar');
  
            // Dapatkan informasi pengguna dari Firebase Admin SDK
            const userRecord = await admin.auth().getUserByEmail(email);
  
            if (request.auth && !request.auth.isAuthenticated && request.cookieAuth) {
              request.cookieAuth.set({ uid: userRecord.uid });
            }
  
            return { success: true, message: 'Login berhasil', user: userRecord.toJSON() };
          } else {
            console.log('Invalid credentials');
            return { success: false, message: 'Password salah, Invalid Credentials' };
          }
        } else {
          console.log('User does not have customAuthInfo');
          return { success: false, message: 'Invalid credentials' };
        }
      } catch (error) {
        console.error('Error during login:', error);
        return h.response({ success: false, message: 'Login gagal', error: error.message }).code(401);
      }
    },
  });
  // Route untuk signup
  // Route untuk signup
  server.route({
    method: 'POST',
    path: '/signup',
    handler: async (request, h) => {
      const { nama, email, username, password, confirmPassword } = request.payload;

      if (password !== confirmPassword) {
        return { success: false, message: 'Konfirmasi password tidak sesuai' };
      }

      try {
        // Buat hash dari password sebelum menyimpan ke Firebase
        const hashedPassword = await bcrypt.hash(password, 10); // Ganti 10 dengan biaya yang sesuai

        // Simpan informasi khusus untuk otentikasi (contoh: token atau hashed info) di Firebase
        await admin.firestore().collection('users').doc(email).set({
          customAuthInfo: hashedPassword,
        });

        const userRecord = await admin.auth().createUser({
          email,
          password: hashedPassword, // Gunakan hash password
          displayName: nama,
        });

        if (request.auth && !request.auth.isAuthenticated && request.cookieAuth) {
          request.cookieAuth.set({ uid: userRecord.uid });
        }

        return { success: true, message: 'Signup berhasil', user: userRecord.toJSON() };
      } catch (error) {
        console.error('Error during signup:', error.message);
        return h.response({ success: false, message: 'Signup gagal', error: error.message }).code(400);
      }
    },
  });
  
  server.route({
    method: 'POST',
    path: '/forgot-password',
    handler: async (request, h) => {
      try {
        const { email } = request.payload;
  
        // Verifikasi apakah email ada dalam sistem
        const userRecord = await admin.auth().getUserByEmail(email);
  
        // Token unik untuk reset password
        const resetToken = generateUniqueToken();
  
        // Set waktu reset
        const expirationTime = new Date();
        expirationTime.setHours(expirationTime.getHours() + 1); // Add 1 hour
  
        await admin.firestore().collection('passwordResetTokens').doc(email).set({
          token: resetToken,
          expirationTime: expirationTime,
        });
  
        // Link untuk reset token
        const resetLink = `http://localhost:3000/reset-password?token=${resetToken}`;
  
        // Create nodemailer transporter
        const transporter = nodemailer.createTransport({
          service: 'gmail',
          auth: {
            user: 'soilinkidn@gmail.com',
            pass: 'eucp bkjg cvyq egaq', // Use the generated App Password here
          },
        });
  
        // Pengaturan Format Pengiriman Email
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
          email: email, // tambahkan informasi email
        });

        return h.response({ success: true, message: 'Reset password link sent successfully.' });
      } catch (error) {
        console.error('Error during forgot password:', error.message);
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
  
        // Dapatkan informasi reset token dan waktu kadaluarsa dari Firestore
        const resetTokenDoc = await admin.firestore().collection('passwordResetTokens').where('token', '==', token).get();
        if (resetTokenDoc.empty) {
          return h.response({ success: false, message: 'Token reset password tidak valid atau telah kadaluarsa.' });
        }
  
        const resetTokenData = resetTokenDoc.docs[0].data();
        const expirationTime = resetTokenData.expirationTime.toDate();
  
        if (expirationTime < new Date()) {
          return h.response({ success: false, message: 'Token reset password telah kadaluarsa.' });
        }
  
        // Tampilkan halaman reset password
        return h.file(Path.join(__dirname, 'views', 'reset-password.html'));
      } catch (error) {
        console.error('Error during reset password page load:', error.message);
        return h.response({ success: false, message: 'Gagal memuat halaman reset password.' }).code(500);
      }
    },
  });
  
  server.route({
    method: 'POST',
    path: '/reset-password',
    handler: async (request, h) => {
      try {
        const { token, newPassword, confirmNewPassword } = request.payload;
  
        // Dapatkan informasi reset token dan waktu kadaluarsa dari Firestore
        const resetTokenDoc = await admin.firestore().collection('passwordResetTokens').where('token', '==', token).get();
        if (resetTokenDoc.empty) {
          return h.response({ success: false, message: 'Token reset password tidak valid atau telah kadaluarsa.' });
        }
  
        const resetTokenData = resetTokenDoc.docs[0].data();
        const expirationTime = resetTokenData.expirationTime.toDate();
        const email = resetTokenData.email; // Definisikan variabel email
  
        if (expirationTime < new Date()) {
          return h.response({ success: false, message: 'Token reset password telah kadaluarsa.' });
        }
  
        if (newPassword === confirmNewPassword) {
          // Update password khusus untuk otentikasi di Firestore
          const hashedNewPassword = await bcrypt.hash(newPassword, 10);
  
          // Gantilah email dengan email yang digunakan pada saat membuat reset token
          console.log('Email:', email);
          const userDoc = await admin.firestore().collection('users').doc(email).get();
          await admin.firestore().collection('users').doc(email).set({
            customAuthInfo: hashedNewPassword,
          }, { merge: true }); // Menggunakan merge: true untuk menggabungkan data dengan dokumen yang sudah ada
  
          // Hapus reset token dari Firestore setelah digunakan
          await admin.firestore().collection('passwordResetTokens').doc(email).delete();
  
          return { success: true, message: 'Password berhasil direset.' };
        } else {
          return { success: false, message: 'Konfirmasi password baru tidak sesuai.' };
        }
      } catch (error) {
        console.error('Error during password reset:', error.message);
        return h.response({ success: false, message: 'Reset password gagal', error: error.message }).code(400);
      }
    },
  });
  
  server.route({
    method: 'GET',
    path: '/login/google',
    handler: (request, h) => {
      // Redirect ke URL otorisasi Google
      const redirectUrl = `https://accounts.google.com/o/oauth2/auth?client_id=${client.options.clientId}&redirect_uri=${encodeURIComponent('http://localhost:3000/login/google/callback')}&response_type=code&scope=email%20profile&access_type=offline`;
      return h.redirect(redirectUrl);
    },
  });
  
  server.route({
    method: 'GET',
    path: '/login/google/callback',
    handler: async (request, h) => {
      try {
        const { code } = request.query;
  
        // Dapatkan token akses dari kode otorisasi
        const tokenResponse = await client.getToken({ code });
        const { id_token } = tokenResponse.tokens;
  
        // Verifikasi token ID Google menggunakan Firebase Authentication
        const ticket = await client.verifyIdToken({
          idToken: id_token,
          audience: '985911723534-nt20v2sijh9s2e9qnbjfv36dpgkh9q5c.apps.googleusercontent.com', // Ganti dengan ID Klien Google Anda
        });
  
        const payload = ticket.getPayload();
        const googleUserId = payload['sub']; // ID unik pengguna Google
  
        // Lakukan login dengan menggunakan googleUserId, misalnya menyimpan ke Firebase Authentication
        // Anda dapat menggabungkannya dengan logika login yang ada di aplikasi Anda
  
        return h.response({ success: true, message: 'Login dengan Google berhasil' }).code(200);
      } catch (error) {
        console.error('Error during Google login:', error.message);
        return h.response({ success: false, message: 'Login dengan Google gagal' }).code(401);
      }
    },
  });


  server.route({
    method: '*',
    path: '/{any*}',
    handler: (request, h) => {
      return h.response({ success: false, message: 'Rute tidak ditemukan' }).code(404);
    },
  });

  server.ext('onPreResponse', (request, h) => {
    const response = request.response;
    if (response.isBoom) {
      console.error('Error:', response.message);
      return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
    }
    return h.continue;
  });

  await server.start();
  console.log('Server berjalan di', server.info.uri);
};

process.on('unhandledRejection', (err) => {
  console.log(err);
  process.exit(1);
});

init();

