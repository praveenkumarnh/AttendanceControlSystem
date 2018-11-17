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
    plugins: [
        new CleanWebpackPlugin([dist]),
        new HtmlWebpackPlugin({
            title: 'Attendance Control System'
        })
    ],
    output: {
        filename: '[name].bundle.js',
        path: path.resolve(__dirname, dist)
    },
    resolve: {
        extensions: ['.js']
    },
    module: {
        rules: [
            {
                test: /\.css$/,
                use: ['style-loader', 'css-loader']
            }
        ],
    },
    stats: {
        colors: true
    },
};