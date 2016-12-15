# Notes
// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }


// http://stackoverflow.com/questions/26602611/how-to-understand-traverse-traverseu-and-traversem
// sequence turns F[G[_]] -> G[F[_]]
// traverse does a map andThen sequence
// traverseM(f) is equivalent to traverse(f).map(_.join), where join is the scalaz name for flatten. It's useful as a kind of "lifting flatMap":
