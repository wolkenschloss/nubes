export const base = {
    get url() {
        return process.env.BASE_URL.replace(/^(.+?)\/*?$/, "$1");
    }
}
