export function Footer() {
  return (
    <footer className="bg-gray-900 dark:bg-[#0A0A0A] text-white">
      <div className="max-w-6xl mx-auto px-4 sm:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* Brand */}
          <div className="space-y-4">
            <h3 className="text-xl font-bold font-jetbrains-mono">Resume Analyzer</h3>
            <p className="text-gray-400 text-sm font-jetbrains-mono">
              AI-powered resume analysis and optimization tool.
            </p>
          </div>

          {/* Quick Links */}
          <div className="space-y-4">
            <h4 className="font-semibold font-jetbrains-mono">Quick Links</h4>
            <ul className="space-y-2 text-sm text-gray-400">
              <li><a href="/analyzer" className="hover:text-white transition-colors font-jetbrains-mono">Resume Analyzer</a></li>
              <li><a href="/job-matching" className="hover:text-white transition-colors font-jetbrains-mono">Job Matching</a></li>
              <li><a href="/ats-checker" className="hover:text-white transition-colors font-jetbrains-mono">ATS Checker</a></li>
              <li><a href="/mock-test" className="hover:text-white transition-colors font-jetbrains-mono">Mock Interview</a></li>
            </ul>
          </div>
        </div>

        {/* Bottom */}
        <div className="border-t border-gray-800 pt-8 mt-8 text-center">
          <p className="text-gray-400 text-sm font-jetbrains-mono">
            © {new Date().getFullYear()} Resume Analyzer. All rights reserved.
          </p>
        </div>
      </div>
    </footer>
  );
}