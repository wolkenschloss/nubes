buildscript {

}

plugins {
    idea
}

idea {
    module {
    // Das funktioniert, macht aber keinen Sinn. Die Build Verzeichnisse und
    // Dateien sollten über Scopes ausgeblendet werden. Ich lasse es als
    // Beispiel hier stehen, damit das in einem anderen Zusammenhang vielleicht
    // benutzt werden kann.
//        excludeDirs.plusAssign(files(
//            ".git-hooks",
//            ".github",
//            ".run",
//            "gradle"))
    }
}
