import { BarChart, Bar, XAxis, YAxis, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import { CheckCircle, XCircle, AlertTriangle, TrendingUp, Download, Upload, Home } from 'lucide-react';
import { downloadReportPdf } from '../../utils/downloadReportPdf';

export function AnalysisResults({ analysis, onReset }) {
  const { atsScore, skillMatch, missingKeywords, suggestions, summary, strengths, weaknesses } = analysis;

  // Data for charts
  const scoreData = [
    { name: 'ATS Score', value: atsScore, color: '#10B981' },
    { name: 'Skill Match', value: skillMatch, color: '#3B82F6' },
  ];

  const pieData = [
    { name: 'Passed', value: atsScore, color: '#10B981' },
    { name: 'Remaining', value: 100 - atsScore, color: '#E5E7EB' },
  ];

  const getScoreColor = (score) => {
    if (score >= 80) return 'text-green-600 dark:text-green-400';
    if (score >= 60) return 'text-yellow-600 dark:text-yellow-400';
    return 'text-red-600 dark:text-red-400';
  };

  const getScoreBg = (score) => {
    if (score >= 80) return 'bg-green-100 dark:bg-green-900/20';
    if (score >= 60) return 'bg-yellow-100 dark:bg-yellow-900/20';
    return 'bg-red-100 dark:bg-red-900/20';
  };

  const saveAnalysis = () => {
    downloadReportPdf({
      title: 'Resume Analysis Report',
      filePrefix: 'resume-analysis-report',
      data: analysis,
    });
  };

  return (
    <div className="space-y-6">
      {/* Header with Action Buttons */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <h2 className="text-2xl font-bold text-gray-900 dark:text-gray-100 font-jetbrains-mono">
          Analysis Results
        </h2>
        <div className="flex flex-wrap gap-3">
          <button
            onClick={() => window.location.href = '/'}
            className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors text-sm font-jetbrains-mono"
          >
            <Home size={16} />
            <span>Go to Home</span>
          </button>
          {onReset && (
            <button
              onClick={onReset}
              className="flex items-center space-x-2 px-4 py-2 bg-orange-600 text-white rounded-lg font-medium hover:bg-orange-700 transition-colors text-sm font-jetbrains-mono"
            >
              <Upload size={16} />
              <span>Try Another Resume</span>
            </button>
          )}
          <button
            onClick={saveAnalysis}
            className="flex items-center space-x-2 px-4 py-2 bg-gray-900 dark:bg-gray-100 text-white dark:text-gray-900 rounded-lg font-medium hover:bg-gray-800 dark:hover:bg-gray-200 transition-colors text-sm font-jetbrains-mono"
          >
            <Download size={16} />
            <span>Save Report</span>
          </button>
        </div>
      </div>

      {/* Score Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div className={`rounded-xl p-6 ${getScoreBg(atsScore)}`}>
          <div className="flex items-center justify-between mb-2">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100 font-jetbrains-mono">ATS Score</h3>
            <TrendingUp className={getScoreColor(atsScore)} size={20} />
          </div>
          <div className={`text-3xl font-bold ${getScoreColor(atsScore)} font-jetbrains-mono`}>
            {atsScore}/100
          </div>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1 font-jetbrains-mono">
            {atsScore >= 80 ? 'Excellent' : atsScore >= 60 ? 'Good' : 'Needs Improvement'}
          </p>
        </div>

        <div className={`rounded-xl p-6 ${getScoreBg(skillMatch)}`}>
          <div className="flex items-center justify-between mb-2">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100 font-jetbrains-mono">Skill Match</h3>
            <TrendingUp className={getScoreColor(skillMatch)} size={20} />
          </div>
          <div className={`text-3xl font-bold ${getScoreColor(skillMatch)} font-jetbrains-mono`}>
            {skillMatch}%
          </div>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-1 font-jetbrains-mono">
            {skillMatch >= 80 ? 'Excellent Match' : skillMatch >= 60 ? 'Good Match' : 'Room for Improvement'}
          </p>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Bar Chart */}
        <div className="bg-white dark:bg-[#262626] rounded-xl p-6">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-4 font-jetbrains-mono">Score Breakdown</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={scoreData}>
              <XAxis dataKey="name" tick={{ fontSize: 12 }} />
              <YAxis domain={[0, 100]} tick={{ fontSize: 12 }} />
              <Bar dataKey="value" radius={[4, 4, 0, 0]}>
                {scoreData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Pie Chart */}
        <div className="bg-white dark:bg-[#262626] rounded-xl p-6">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-4 font-jetbrains-mono">ATS Compatibility</h3>
          <ResponsiveContainer width="100%" height={200}>
            <PieChart>
              <Pie
                data={pieData}
                dataKey="value"
                nameKey="name"
                cx="50%"
                cy="50%"
                innerRadius={40}
                outerRadius={80}
                paddingAngle={5}
              >
                {pieData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
            </PieChart>
          </ResponsiveContainer>
          <div className="text-center mt-2">
            <span className="text-2xl font-bold text-gray-900 dark:text-gray-100 font-jetbrains-mono">{atsScore}%</span>
            <p className="text-sm text-gray-600 dark:text-gray-400 font-jetbrains-mono">ATS Compatible</p>
          </div>
        </div>
      </div>

      {/* Professional Summary */}
      {summary && (
        <div className="bg-white dark:bg-[#262626] rounded-xl p-6">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-4 font-jetbrains-mono">AI-Generated Summary</h3>
          <p className="text-gray-700 dark:text-gray-300 leading-relaxed font-jetbrains-mono">{summary}</p>
        </div>
      )}

      {/* Missing Keywords */}
      {missingKeywords && missingKeywords.length > 0 && (
        <div className="bg-white dark:bg-[#262626] rounded-xl p-6">
          <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-4 font-jetbrains-mono">Missing Keywords</h3>
          <div className="flex flex-wrap gap-2">
            {missingKeywords.map((keyword, index) => (
              <span
                key={index}
                className="px-3 py-1 bg-orange-100 dark:bg-orange-900 text-orange-800 dark:text-orange-200 text-sm rounded-full font-jetbrains-mono"
              >
                {keyword}
              </span>
            ))}
          </div>
          <p className="text-sm text-gray-600 dark:text-gray-400 mt-4 font-jetbrains-mono">
            Consider adding these keywords to improve your resume's relevance.
          </p>
        </div>
      )}

      {/* Strengths & Weaknesses */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {strengths && strengths.length > 0 && (
          <div className="bg-white dark:bg-[#262626] rounded-xl p-6">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-4 font-jetbrains-mono flex items-center">
              <CheckCircle className="text-green-500 mr-2" size={20} />
              Strengths
            </h3>
            <ul className="space-y-2">
              {strengths.map((strength, index) => (
                <li key={index} className="flex items-start space-x-2">
                  <div className="w-2 h-2 bg-green-500 rounded-full flex-shrink-0 mt-2"></div>
                  <span className="text-gray-700 dark:text-gray-300 text-sm font-jetbrains-mono">{strength}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {weaknesses && weaknesses.length > 0 && (
          <div className="bg-white dark:bg-[#262626] rounded-xl p-6">
            <h3 className="font-semibold text-gray-900 dark:text-gray-100 mb-4 font-jetbrains-mono flex items-center">
              <AlertTriangle className="text-yellow-500 mr-2" size={20} />
              Areas for Improvement
            </h3>
            <ul className="space-y-2">
              {weaknesses.map((weakness, index) => (
                <li key={index} className="flex items-start space-x-2">
                  <div className="w-2 h-2 bg-yellow-500 rounded-full flex-shrink-0 mt-2"></div>
                  <span className="text-gray-700 dark:text-gray-300 text-sm font-jetbrains-mono">{weakness}</span>
                </li>
              ))}
            </ul>
          </div>
        )}
      </div>

      {/* Suggestions */}
      {suggestions && (
        <div className="bg-blue-50 dark:bg-blue-900/20 rounded-xl p-6">
          <h3 className="font-semibold text-blue-900 dark:text-blue-200 mb-4 font-jetbrains-mono">AI Recommendations</h3>
          <p className="text-blue-800 dark:text-blue-300 leading-relaxed font-jetbrains-mono">{suggestions}</p>
        </div>
      )}
    </div>
  );
}