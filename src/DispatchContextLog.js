import React, { createContext, useState } from 'react';

export const DispatchLogContext = createContext();

export const DispatchLogProvider = ({ children }) => {
  const [dispatchLogs, setDispatchLogs] = useState([]);
  return (
    <DispatchLogContext.Provider value={{ dispatchLogs, setDispatchLogs }}>
      {children}
    </DispatchLogContext.Provider>
  );
};
