buildscript {

}

plugins {
    idea
}

idea {
    module {
        // Das funktioniert, macht aber keinen Sinn.
        // Die Build Verzeichnisse sollten über Scopes
        // ausgeblendet werden.
//        excludeDirs.plusAssign(files(
//            ".git-hooks",
//            ".github",
//            ".run",
//            "gradle"))
    }
}