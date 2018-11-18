import jQuery from 'jquery';
import { Tracks } from "../tracks";
import view from "html-loader!./index.html";
import "./index.css";

export default class Home  {
    static render(){
        jQuery("body").append(view);
        Tracks.render();
    }
}