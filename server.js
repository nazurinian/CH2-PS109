const Hapi = require('@hapi/hapi');
const admin = require('firebase-admin');
const Path = require('path');
const Multer = require('multer');
const { Storage } = require('@google-cloud/storage');
const bcrypt = require('bcrypt'); 
const saltRounds = 10; 
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
    method: 'GET',
    path: '/login',
    handler: (request, h) => {
      try {
        // Logika autentikasi, jika diperlukan
        
        // Jika autentikasi berhasil
        // return h.response({ success: true }).code(200);
        
        // Jika autentikasi gagal
        return h.response({ success: false, message: 'Autentikasi gagal' }).code(401);
        
      } catch (error) {
        console.error(error);
        return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
      }
    },
  });
  
  server.route({
    method: 'GET',
    path: '/signup',
    handler: (request, h) => {
      try {
        // Logika pendaftaran, jika diperlukan
        
        // Jika pendaftaran berhasil
        //return h.response({ success: true }).code(200);
        
        // Jika pendaftaran gagal
        return h.response({ success: false, message: 'Pendaftaran gagal' }).code(403);
        
      } catch (error) {
        console.error(error);
        return h.response({ success: false, message: 'Terjadi kesalahan internal' }).code(500);
      }
    },
  });

  server.route({
    method: 'POST',
    path: '/login',
    handler: async (request, h) => {
      const { email, password } = request.payload;
  
      try {
        if (!email || !password) {
          return h.response({ success: false, message: 'Email dan password harus diisi' }).code(400);
        }
  
        const userRecord = await admin.auth().getUserByEmail(email);
        const hashedPassword = userRecord.passwordHash || ''; // Ambil hash password dari Firebase (jika ada)

        // Verifikasi password menggunakan bcrypt
        const isPasswordMatch = await bcrypt.compare(password, hashedPassword);

        if (!isPasswordMatch) {
          // Handle ketika password tidak sesuai
          return h.response({ success: false, message: 'Login gagal: Password salah' }).code(401);
        }

        // Jika password sesuai, set session dengan ID pengguna
        if (request.auth && !request.auth.isAuthenticated && request.auth.artifacts) {
          request.auth.artifacts = { uid: userRecord.uid };
        }
        
        return { success: true, message: 'Login berhasil', user: userRecord.toJSON() };
      } catch (error) {
        if (error.code === 'auth/user-not-found') {
          // Handle ketika pengguna tidak ditemukan
          return h.response({ success: false, message: 'Login gagal: Pengguna tidak ditemukan' }).code(401);
        } else {
          // Handle kesalahan lainnya
          console.error('Error during login:', error.message);
          return h.response({ success: false, message: 'Login gagal' }).code(401);
        }
      }
    },
  });
  
  server.route({
    method: 'POST',
    path: '/signup',
    handler: async (request, h) => {
      const { nama, email, username, password, confirmPassword } = request.payload;
  
      if (!nama || !email || !username || !password || !confirmPassword) {
        return h.response({ success: false, message: 'Semua kolom harus diisi' }).code(400);
      }
  
      if (password !== confirmPassword) {
        return { success: false, message: 'Konfirmasi password tidak sesuai' };
      }
  
      try {
        const userRecord = await admin.auth().createUser({
          email,
          password,
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

