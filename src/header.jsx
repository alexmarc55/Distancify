import React from 'react';
import { Link } from 'react-router-dom';

const Header = () => {
  return (
    <header className="bg-white shadow-md py-2 px-8 flex items-center justify-between fixed w-full z-500">
      {/* Left side: Logo and Home */}
      <div className="flex items-center space-x-6">
        <img 
          src="./images/logo.png" 
          alt="Logo" 
          className="h-10 w-auto"
        />
        <Link 
          to="/home"
          className="text-lg font-semibold text-gray-800 hover:text-blue-600 transition-colors duration-200"
        >
          Home
        </Link>
      </div>

      {/* Right side: Navigation */}
      <nav>
        <ul className="flex items-center space-x-6 text-base">
          <li>
            <Link 
              to="/lastcalls"
              className="relative text-gray-700 hover:text-blue-600 transition-colors duration-200 after:absolute after:left-0 after:-bottom-1 after:w-0 after:h-0.5 after:bg-blue-500 hover:after:w-full after:transition-all after:duration-300"
            >
              Last Calls
            </Link>
          </li>
          <li>
            <Link 
              to="/"
              className="bg-red-500 text-white px-4 py-1.5 rounded-md font-medium hover:bg-red-600 transition-colors duration-200 shadow-sm"
            >
              Log Out
            </Link>
          </li>
        </ul>
      </nav>
    </header>
  );
};

export default Header;
