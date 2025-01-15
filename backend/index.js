const io = require('socket.io')(3002, {
    cors: {
        origin: "*",
    },
});

io.on('connection', (socket) => {
    console.log('A user connected');

    socket.on('message', (data) => {
        console.log(`Message received: ${data}`);
        socket.emit('message', `Echo: ${data}`);
    });

    socket.on('disconnect', () => {
        console.log('A user disconnected');
    });
});
