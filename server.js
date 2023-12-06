const Hapi = require('@hapi/hapi');
const admin = require('firebase-admin');
const Path = require('path');

const firebaseConfig = require('./firebaseConfig.json');

admin.initializeApp({
  credential: admin.credential.cert(firebaseConfig),
});

const init = async () => {
  const server = Hapi.server({
    port: 3000,
    host: 'localhost',
  });

  await server.register(require('@hapi/inert'));

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
    method: 'POST',
    path: '/login',
    handler: async (request, h) => {
      const { email, password } = request.payload;
  
      try {
        const userRecord = await admin.auth().getUserByEmail(email);
        // Verifikasi password di sini (Anda dapat menggunakan library seperti bcrypt)
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

