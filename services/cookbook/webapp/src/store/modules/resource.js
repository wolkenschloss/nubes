import {parseTemplate} from "url-template";

export class Resource {
    constructor(template) {
        this.template = parseTemplate(template)
    }

    static get baseUrl() {
        return process.env.BASE_URL.replace(/^(.+?)\/*?$/, "$1");
    }

    url(context) {
        return this.template.expand({baseUrl: Resource.baseUrl, ...context})
    }
}
