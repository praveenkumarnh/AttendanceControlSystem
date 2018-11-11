/*jshint esversion: 6 */

const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CleanWebpackPlugin = require('clean-webpack-plugin');
const webpack = require('webpack');
const dist = path.resolve(__dirname, 'dist');

module.exports = {
    entry: {
        app: ['./src/index.js']
    },
    optimization: {
        splitChunks: {
            chunks: 'all'
        }
    },
    devtool: 'inline-source-map',
    plugins: [
        new CleanWebpackPlugin([dist]),
        new HtmlWebpackPlugin({
            title: 'Attendance Control System'
        })
    ],
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, dist)
    },
    resolve: {
        alias: {
            'vertx-eventbus': path.resolve(__dirname, 'src/vertx-eventbus.js'),
            'sockjs': path.resolve(__dirname, 'src/sockjs.js')
        }
    },
    module:{
        rules:[
            {
                test:/\.css$/,
                use:['style-loader','css-loader']
            }
        ]
    },
};