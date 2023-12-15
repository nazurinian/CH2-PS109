const Hapi = require('@hapi/hapi');
const admin = require('firebase-admin');
const Path = require('path');
const Multer = require('multer');
const bcrypt = require('bcrypt');
const { Storage } = require('@google-cloud/storage');
const nodemailer = require('nodemailer');
const validator = require('validator');
const port = process.env.PORT || 5000;

const storage = new Storage({
  keyFilename: 'soilink.json', // Ganti dengan path file kredensial Anda
  projectId: 'soilink',
});

const bucket = storage.bucket('staging.soilink.appspot.com');

const firebaseConfig = require('./firebaseConfig.json');

admin.initializeApp({
  credential: admin.credential.cert(firebaseConfig),
});

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


  // server.route({
  //   method: 'GET',
  //   path: '/login',
  //   handler: (request, h) => {
  //     return h.file(Path.join(__dirname, 'views', 'login.html'));
  //   },
  // });

  // server.route({
  //   method: 'GET',
  //   path: '/signup',
  //   handler: (request, h) => {
  //     return h.file(Path.join(__dirname, 'views', 'signup.html'));
  //   },
  // });

  server.route({
    method: 'POST',
    path: '/login',
    handler: async (request, h) => {
      try {
        const { email, password } = request.payload;
  
        console.log('Login attempt for email:', email);
        
        //password dan email harus diisi
        if (!email || !password) {
          return h.response({ success: false, message: 'Email dan password harus diisi' }).code(400);
        }
  
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

  // server.route({
  //   method: 'POST',
  //   path: '/login',
  //   handler: async (request, h) => {
  //     const { email, password } = request.payload;
  
  //     try {
  //       if (!email || !password) {
  //         return h.response({ success: false, message: 'Email dan password harus diisi' }).code(400);
  //       }
  //       const userRecord = await admin.auth().getUserByEmail(email);
  //       // Verifikasi password di sini (Anda dapat menggunakan library seperti bcrypt)
  //       // Jika password sesuai, set session dengan ID pengguna
  //       if (request.auth && !request.auth.isAuthenticated && request.auth.artifacts) {
  //         request.auth.artifacts = { uid: userRecord.uid };
  //       }
                
  //       return { success: true, message: 'Login berhasil', user: userRecord.toJSON() };
  //     } catch (error) {
  //       if (error.code === 'auth/user-not-found') {
  //         // Handle ketika pengguna tidak ditemukan
  //         return h.response({ success: false, message: 'Login gagal: Pengguna tidak ditemukan' }).code(401);
  //       } else {
  //         // Handle kesalahan lainnya
  //         console.error('Error during login:', error.message);
  //         return h.response({ success: false, message: 'Login gagal' }).code(401);
  //       }
  //     }
  //   },
  // });
  
  // server.route({
  //   method: 'POST',
  //   path: '/signup',
  //   handler: async (request, h) => {
  //     const { nama, email, username, password, confirmPassword } = request.payload;
  
  //     if (password !== confirmPassword) {
  //       return { success: false, message: 'Konfirmasi password tidak sesuai' };
  //     }
  
  //     try {
  //       const userRecord = await admin.auth().createUser({
  //         email,
  //         password,
  //         displayName: nama,
  //       });
  
  //       if (request.auth && !request.auth.isAuthenticated && request.cookieAuth) {
  //         request.cookieAuth.set({ uid: userRecord.uid });
  //       }
  
  //       return { success: true, message: 'Signup berhasil', user: userRecord.toJSON() };
  //     } catch (error) {
  //       console.error('Error during signup:', error.message);
  //       return h.response({ success: false, message: 'Signup gagal', error: error.message }).code(400);
  //     }
  //   },
  // });


// ...

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
      const resetLink = `https://soilink.et.r.appspot.com//reset-password?token=${resetToken}`;

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

      return h.response({ success: true, message: 'Reset password link sent successfully.' });
    } catch (error) {
      console.error('Error during forgot password:', error.message);
      return h.response({ success: false, message: 'Failed to process forgot password request.' }).code(500);
    }
  },
});

  // Fungsi untuk menghasilkan token unik
  function generateUniqueToken() {
    const timestamp = new Date().getTime();
    const randomChars = Math.random().toString(36).substring(2, 8);
    return `${timestamp}-${randomChars}`;
  }


  
  server.route({
    method: 'POST',
    path: '/reset-password',
    handler: async (request, h) => {
      try {
        const { email, newPassword } = request.payload;
  
        // Validasi Format Email
        if (!isValidEmail(email)) {
          return h.response({ success: false, message: 'Invalid email format' }).code(400);
        }
  
        if (typeof newPassword === 'undefined' || newPassword.trim() === '') {
          return h.response({ success: false, message: 'New password is missing or empty' }).code(400);
        }
  
        // Dapatkan informasi pengguna berdasarkan email
        const userRecord = await admin.auth().getUserByEmail(email);
  
        // Update password pengguna
        await admin.auth().updateUser(userRecord.uid, {
          password: newPassword,
        });
  
        return h.response({ success: true, message: 'Password reset successfully.' });
      } catch (error) {
        console.error('Error during password reset:', error.message);
        return h.response({ success: false, message: 'Failed to reset password.' }).code(500);
      }
    },
  });
    function isValidEmail(email) {
      return validator.isEmail(email);
    }
  
  
  
 
  
  server.route({
    method: 'POST',
    path: '/upload',
    handler: async (request, h) => {
      try {
        // Menangkap file dari formulir HTML dengan nama 'image'
        const file = request.payload.image;

        // Mengekstrak informasi file
        const fileName = file.hapi.filename;
        const data = file._data;

        // Membuat stream file
        const blob = bucket.file(fileName);
        const blobStream = blob.createWriteStream({
          resumable: false,
          metadata: {
            contentType: file.hapi.headers['content-type'],
          },
        });

        blobStream.on('error', (err) => {
          console.error(err);
          return h.response({ success: false, message: 'Gagal mengunggah gambar' }).code(500);
        });

        blobStream.on('finish', async () => {
          // Mendapatkan URL publik gambar
          const publicUrl = `https://storage.googleapis.com/${bucket.name}/${blob.name}`;

          return h.response({ success: true, message: 'Gambar berhasil diunggah', imageUrl: publicUrl });
        });

        // Menulis data ke stream file
        blobStream.end(data);
      } catch (error) {
        console.error(error);
        return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
      }
    },
    options: {
      payload: {
        output: 'stream',
        allow: 'multipart/form-data',
        parse: true,
        maxBytes: 2 * 1024 * 1024, // Batas ukuran file (2 MB)
      },
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

