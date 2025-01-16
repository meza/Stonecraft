module.exports = function (context, options) {
    return {
        name: 'custom-loaders',
        configureWebpack(config, isServer) {
            return {
                devServer: {
                    headers: {
                        "Access-Control-Allow-Origin": "*",
                        "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, PATCH, OPTIONS",
                        "Access-Control-Allow-Headers": "X-Requested-With, content-type, Authorization"
                    }
                },
                module: {
                    rules: [
                        {
                            test: /\.properties$/i,
                            use: [
                                {
                                    loader: 'properties-file/webpack-loader',
                                },
                            ],
                        },
                    ],
                },
            };
        },
    };
};
