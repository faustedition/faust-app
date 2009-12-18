Practical XML - Providing What The JDK Doesn't

    This library is designed to be an easy-to-use wrapper around the XML
    support built-in to the JDK. It is based on the idea that the JDK
    implementations will live forever and be regularly updated, something
    that cannot be said about third-party alternatives.

Licensing and Copyright

    This library is released under the Apache 2 license:
    http://www.apache.org/licenses/LICENSE-2.0

    Copyright is retained by the individual maintainers, for the code
    that they have contributed. In the case of patch submissions from
    third parties, the patch contributor implicitly assigns copyright
    to the maintainer who implements the patch.

Release Numbering

    All builds post initial release have a three part revision number:
    MAJOR.MINOR.PATCH

        MAJOR releases are not required to be backwards compatible with
              the previous such release. For example, the 1.0.0 release
              has a different package structure than the 0.9.0 release.
              It is intended that there will never be a change to the
              major release number, although "never" is a long time.
        MINOR releases will happen whenever a maintainer decides that
              one is needed, typically driven by significant new
              functionality and a desire to have the release in a central
              repository. Minor releases preserve backwards compatibility:
              code written for one minor release will work when linked
              with another minor release.
        PATCH releases happen whenever a method or class gets added or
              changed in a minor way. They will not be released to any
              central repository, and will probably not be tagged either.

    Major and minor releases will be tagged in the Subversion repository,
    using the form "rel-X.Y", where X and Y are the major and minor release
    numbers. Patch releases may be tagged using the form "rel-X.Y.Z", if the
    maintainer decides that it's important to rebuild that particular release
    (eg, as a dependency for another application).

    Major and minor releases will be available for download from Sourceforge
    ( http://sourceforge.net/project/showfiles.php?group_id=234884 ), and also
    from the Central Maven Repository.

    The whole "patch release" idea has caused some controversy. My thought with
    these releases is that they're a step above "nightly development build,"
    and represent stable functionality. By giving fixed release numbers, your
    build tool should call out any possible incompatibilities, such as "your
    main project relies on 1.0.15, but your repository contains 1.0.17, why?"
    I believe that long-lived "snapshot" builds on the trunk are a bad idea
    (that said, if you're making a number of changes and want to check-in
    increments, feel free to put "SNAPSHOT" on the revision number; but try
    to finish within a day).

    As far as tagging goes, I don't think that it's necessary unless you rely
    on a particular patch revision. Then go for it. Since Subversion revision
    numbers apply to the entire repository, you can always create an tag for
    some past point in time. Easiest way to do this is browse the history of
    pom.xml.

    To clear up one last bit of confusion: the patch number should only be
    incremented when the external API has changed. If you add comments, or
    tests, or even completely rewrite the internals, the patch number stays
    the same. 
